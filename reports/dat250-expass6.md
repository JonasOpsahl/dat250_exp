# Report: Experiment 6 - DAT250

This report details the problems I encounted during the implementation of kafka as a message broker into my app. The goal was to register a new Kafka topic for each poll created, allow votes to be cast by publishing events to these topics, and have the PollApp consume these events to update the database.

---

## Technical problems encountered

### 1. Apache kafka setup on NixOS

An initial challenge was setting up Apache Kafka on NixOS. Official documentation and community guides for this specific combination were not very plentiful. But since I have heard that Kafka is used a lot in the industry, and I have some experience with it from before I chose to persist because I figured it would be worth learning.

My first consideration was to use Docker for a containerized setup, which would abstract away host system dependencies. But since we are using Docker next week anyway, I figured I would do it another way and installed it natively.

After looking at the NixOS community forums, I found a working [configuration](https://discourse.nixos.org/t/how-to-setup-kafka-server-on-nixos/45055) that leverages KRaft mode. This approach conveniently eliminates the need for a separate Zookeeper instance, simplifying the overall architecture. The following configuration was added to my NixOS `configuration.nix` to deploy a single-node KRaft cluster:

```nix
services.apache-kafka = {
  enable = true;
  clusterId = "your-generated-cluster-id"; # Generated using the kafka-storage.sh tool
  formatLogDirs = true;
  settings = {
    listeners = [
      "PLAINTEXT://:9092"
      "CONTROLLER://:9093"
    ];
    "listener.security.protocol.map" = [
      "PLAINTEXT:PLAINTEXT"
      "CONTROLLER:PLAINTEXT"
    ];
    "controller.quorum.voters" = [
      "1@127.0.0.1:9093"
    ];
    "controller.listener.names" = ["CONTROLLER"];
    "node.id" = 1;
    "process.roles" = ["broker" "controller"];
    "log.dirs" = ["/var/lib/apache-kafka"];
    "offsets.topic.replication.factor" = 1;
    "transaction.state.log.replication.factor" = 1;
    "transaction.state.log.min.isr" = 1;
  };
};
```
This declarative setup ensures that Kafka runs as a systemd service automatically, which proved to be a good enough solution once configured.

### 2. Investigating polling latency

After successfully integrating the Kafka producer and consumer, I observed a noticeable processing delay when casting votes. My initial hypothesis was that this latency was caused by a single consumer having to switch between different Kafka topics, as the delay was most obvious when voting on different polls sequentially.

However, this theory was disproven when I realized the same delay occurred when voting for different options within the same poll (which have the same topic). This pointed to a different cause.

The actual issue stems from the architectural shift from a synchronous to an asynchronous model. And is quite obvious after thinking twice about it. By introducing Kafka, the REST API no longer waits for the database transaction to complete. It hands the message to the broker and immediately returns a success response. This change exposed two underlying challenges:

1.  **Backend processing latency:** The delay itself is happening at the application/database level. The first time a vote is cast for a specific poll option, the persistence layer (JPA/Hibernate) must perform a "cold" read and write from the database, which is a slow operation. Subsequent votes for the same option are much faster as the relevant data is now "warm" and held in a cache.

2.  **Frontend communication disconnect:** In the old synchronous model, the users browser was forced to wait for the database operation, but it would then receive the updated results instantly. Now, because the API returns immediately, the user is disconnected from the backend process. They don't receive an automatic update, making the processing delay much more apparent and forcing a manual refresh to see the final result.

While this behavior is acceptable for a small scale application, the lack of real time feedback would create a poor user experience in a production environment with many active polls. This is a key area for future improvement, as discussed in the next section.

---

Also, I was not quite sure what was meant about "test your set-up by connecting directly to the message broker with a standalone application", but I was able to connect to the broker using the terminal like this:

```bash
jonas@nixos:~/Documents/Uni/master/pu1/dat250/dat250_exp/ > nix-shell -p apacheKafka --extra-experimental-features nix-command --extra-experimental-features flakes
this path will be fetched (0.02 MiB download, 0.06 MiB unpacked):
  /nix/store/p2mnji2cdxgf6h27hlqzqf7g8f9bqfsi-stdenv-linux
copying path '/nix/store/p2mnji2cdxgf6h27hlqzqf7g8f9bqfsi-stdenv-linux' from 'https://cache.nixos.org'...
```
In this shell I could use different commands to look at for example the groups:

```bash
[nix-shell:~/Documents/Uni/master/pu1/dat250/dat250_exp]$ kafka-consumer-groups.sh --bootstrap-server localhost:9092 --list
poll-app
```
And look at the messages sent within each topic:

```bash
[nix-shell:~/Documents/Uni/master/pu1/dat250/dat250_exp]$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic poll.voteChange.1 --from-beginning
{"pollId":1,"presentationOrder":1,"userId":1}
{"pollId":1,"presentationOrder":1,"userId":1}
{"pollId":1,"presentationOrder":1,"userId":1}
{"pollId":1,"presentationOrder":2,"userId":1}
{"pollId":1,"presentationOrder":1,"userId":1}
{"pollId":1,"presentationOrder":2,"userId":null}
{"pollId":1,"presentationOrder":1,"userId":null}
{"pollId":1,"presentationOrder":1,"userId":null}
{"pollId":1,"presentationOrder":2,"userId":null}
{"pollId":1,"presentationOrder":1,"userId":null}
{"pollId":1,"presentationOrder":1,"userId":1}
{"pollId":1,"presentationOrder":2,"userId":null}
{"pollId":1,"presentationOrder":2,"userId":null}
{"pollId":1,"presentationOrder":1,"userId":1}
{"pollId":1,"presentationOrder":2,"userId":1}
{"pollId":1,"presentationOrder":1,"userId":null}
{"pollId":1,"presentationOrder":2,"userId":1}
{"pollId":1,"presentationOrder":2,"userId":null}
{"pollId":1,"presentationOrder":1,"userId":null}
{"pollId":1,"presentationOrder":2,"userId":null}
{"pollId":1,"presentationOrder":1,"userId":null}
{"pollId":1,"presentationOrder":2,"userId":null}
{"pollId":1,"presentationOrder":1,"userId":1}
{"pollId":1,"presentationOrder":2,"userId":null}
{"pollId":1,"presentationOrder":2,"userId":1}
{"pollId":1,"presentationOrder":1,"userId":1}
{"pollId":1,"presentationOrder":1,"userId":null}
Processed a total of 27 messages
```

I could also send messages from the terminal without actually voting through the webapp:
```bash
kafka-console-producer.sh --bootstrap-server localhost:9092 --topic poll.voteChange.1
# Then you will be prompted to enter something, you should enter what your application excepts. In make case this would be something like this, although you can add a userId aswell 
{"pollId":1,"presentationOrder":1}
```
---

The first time I entered these values into the producer in the terminal I was a bit clueless. At first I just pressed enter a few times and then I sent the wrong format. Turns out my app wasnt configured for this, and everything crashed. Even after rebooting my pc, restarting vscode nothing worked. Whenever I tried to start my backend it would just crash. So I did a bit of research and figured out that the most likely cause was a "poison pill message". But to reset this I used the following command:
```bash
onas@nixos:~/Documents/Uni/master/pu1/dat250/dat250_exp/ > nix --extra-experimental-features nix-command --extra-experimental-features flakes shell 'nixpkgs#legacyPackages.x86_64-linux.apacheKafka' --command kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group poll-app --topic poll.voteChange.1 --reset-offsets --to-latest --execute

GROUP                          TOPIC                          PARTITION  NEW-OFFSET     
poll-app                       poll.voteChange.1              0          36       
```
And after that everything worked correctly again.
I also made a simple KafkaTester class in the test folder. If you run the script you can easily send messages and listen to topics through it, instead of voting through the webapp or look in the database.

---

## Pending issues and future improvements

### 1. Asynchronous flow and real time updates

The introduction of Kafka shifted the applications architecture from synchronous to asynchronous. Previously, the REST API would handle a vote request, update the database, and then return a confirmation to the user in a single, blocking operation.

Now, the API endpoint simply publishes a "vote" event to a Kafka topic and immediately returns a `200 OK` response. The actual database update is handled later by the Kafka consumer. This is a good design, but it creates a disconnect for the end-user, who no longer receives immediate confirmation that their vote has been counted and the poll results have been updated.

A potential solution is to implement WebSockets. The workflow would then be:
1.  A user votes via the REST API.
2.  The server publishes the vote event to Kafka.
3.  The Kafka consumer processes the event and updates the database.
4.  After the database update, the server pushes the new poll results to all subscribed clients via a WebSocket connection.

This would provide the real time feedback that is currently missing and would be a valuable future improvement. Also, a real time application is much cooler than the one I have now so this is definelty something I will implement later.

### 2. Security: Password hashing

A critical security feature that remains unimplemented is password hashing. Currently, user passwords are likely stored in plaintext, which is not very good. This is something I find interesting so I would be looking to implement this with an algorithm like BCrypt for example.