# Report: Experiment 5 - DAT250

This report details the implementation of Redis for two primary use cases: tracking logged in users and caching more complex objects to learn the concepts of caching for improving application performance. It also covers the technical challenges encountered and the solutions implemented.

---

## Tracking Logged-in Users

To manage the state of logged-in users, the Redis **Set** data type was chosen. Sets are ideal for this task because they store unique, unordered elements, naturally preventing a user from being listed as logged-in multiple times.

### CLI Simulation

The following sequence of `redis-cli` commands was used to simulate user login and logout events:

1.  **Initial State Check**: Checked for members in a non-existent set.
    ```sh
    SMEMBERS loggedin
    ```

2.  **User "alice" logs in**: The `SADD` command adds "alice" to the `loggedin` set.
    ```sh
    SADD loggedin "alice"
    ```

3.  **User "bob" logs in**: "bob" is also added to the set.
    ```sh
    SADD loggedin "bob"
    ```

4.  **Check Current Members**: Using `SMEMBERS` again shows both users.
    ```sh
    SMEMBERS loggedin
    ```

5.  **User "alice" logs out**: The `SREM` command removes "alice" from the set.
    ```sh
    SREM loggedin "alice"
    ```

6.  **User "eve" logs in**: Finally, "eve" is added to the set.
    ```sh
    SADD loggedin "eve"
    ```

The final state showed "bob" and "eve" as the currently logged in users.

---

## More complex information

For representing a complex, nested object like a poll, the Redis JSON data type is a perfect fit. It allows for storing the entire JSON structure and manipulating individual fields within it efficiently.

### CLI Simulation

1.  **Store the Poll Object**: The `JSON.SET` command was used to store the entire poll object under the key `poll:03ebcb7b-bd69-440b-924e-f5b7d664af7b`.
    ```sh
    JSON.SET poll:03ebcb7b-bd69-440b-924e-f5b7d664af7b '$' '{
      "id": "03ebcb7b-bd69-440b-924e-f5b7d664af7b",
      "title": "Pineapple on Pizza?",
      "options": [
          { "caption": "Yes, yammy!", "voteCount": 269 },
          { "caption": "Mamma mia, nooooo!", "voteCount": 268 },
          { "caption": "I do not really care ...", "voteCount": 42 }
      ]
    }'
    ```

2.  **Increment a Vote Count**: Instead of retrieving, modifying, and rewriting the whole object, the `JSON.NUMINCRBY` command atomically increments the `voteCount`. A JSONPath expression (`$.options[1].voteCount`) targets the specific field.
    ```sh
    JSON.NUMINCRBY poll:03ebcb7b-bd69-440b-924e-f5b7d664af7b '$.options[1].voteCount' 1
    # "269" 
    ```

3.  **Verify the Change**: `JSON.GET` was used to retrieve the updated object and confirm the vote count had changed.

---

## Java Implementation with Jedis

The concepts explored on the command line were then implemented in the Java poll app using the **Jedis** library. A `CachingPollService` was created to act as a layer between the application logic and the primary database (`HibernatePollService`), intercepting calls to handle caching.

### User Session Management

The `CachingPollService` implements the logic for tracking logged in users, mapping directly to the Set commands from Use Case 1.

* `loginUser(Integer userId)` uses `jedis.sadd("users:loggedIn", userId.toString())` to add a user to the set.
* `logoutUser(Integer userId)` uses `jedis.srem("users:loggedIn", userId.toString())` to remove a user.
* `isUserLoggedIn(Integer userId)` uses `jedis.sismember(...)` for a quick check.
* `getLoggedInUsers()` uses `jedis.smembers(...)` to retrieve the full set of active users.

### Implementing the Cache for Poll Results

The most common use case for Redis caching—was implemented for poll results to avoid expensive database queries. While the JSON data type was explored, the implementation uses the **Hash** data type for simplicity and efficiency in this specific scenario. The hash stores the poll's results as a flat map of `optionCaption` to `voteCount`.

The caching logic in `getPollResults(Integer pollId)` follows these steps:
1.  **Check Cache First**: It checks if a cache entry exists for the given poll ID using `jedis.exists(cacheKey)`.
2.  **Cache Hit**: If the entry exists, the results are retrieved directly from Redis using `jedis.hgetAll(cacheKey)` and returned, avoiding a database call.
3.  **Cache Miss**: If the entry does not exist, it calls the `delegate` service to query the results from the primary database.
4.  **Populate Cache**: The results from the database are then transformed into a `Map<String, String>` and stored in the Redis hash using `jedis.hset(cacheKey, resultsToCache)`.
5.  **Set TTL**: A **(TTL)** of one hour is set on the cache key using `jedis.expire(cacheKey, CACHE_TTL_SECONDS)`. This ensures that stale data is eventually evicted automatically.

### Cache Invalidation

To maintain data consistency, the cache must be invalidated when the underlying data changes.
* When a vote is cast via `castVote(...)`, the corresponding poll's cache entry is deleted with `jedis.del(getPollCacheKey(pollId))`.
* Similarly, when a poll is deleted via `deletePoll(...)`, its cache entry is also removed.

This write through invalidation strategy ensures that the next request for the poll's results will trigger a cache miss and fetch the fresh data from the database.

---

## Technical Problems Encountered

My primary technical challenge was environmental. Due to extreme weather, my flight from Bordeaux was canceled, significantly delaying my return to Bergen. Fortunately, I had my laptop with me so I could put on the finishing touches and deliver within the deadline.

The main issue was configuring the correct Java version on macOS. My project requires JDK 21, but the system environment was not correctly picking it up. I resolved this by installing OpenJDK 21 via Homebrew and then creating a symbolic link to point the system's Java Virtual Machines directory to the new installation. I usually do all my programming on my desktop at home where I use linux, so this was just a little hinderance.

Also, I got briefly confused when I tried to run my application but the cache wouldnt work. Turns out I had just forgot to start the redis server in a terminal. So that was an easy fix, just running:
```sh
redis-server
```


The following commands were used to fix the issue with wrongly configured Java version:
```sh
# Create the symbolic link
sudo ln -sfn /opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-21.jdk

# Reload the shell configuration to apply changes
source ~/.zshrc

# Verify the correct version was active
java -version
```

Since I didnt encounter any significant problems with this assignment I decided to include what I did in the two cases we were supposed to test out to get familiar with redis. 

---

## Further improvements

The next thing I want to do is add password hashing, I am planning on doing this with Spring Security and BCrypt.