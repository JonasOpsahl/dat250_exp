# Report: Experiment 2 - DAT250

## Installation

In this initial phase of the project I did not encounter any problems with installations or such. My primary operating system is **NixOS**, a Linux distribution that utilizes a declarative approach to system and package management. This choice significantly simplifies the setup process and enhances reproducibility.

## Technical Challenges and Resolutions

The implementation process was largely smooth, with the primary challenges being related to architectural design rather than specific installation or configuration issues. The project underwent two major refactorings to improve the initial design and ensure a more robust and scalable solution.

The most significant technical design decision involved handling user association for votes. The system requirements presented a conflicting scenario:

- The data model diagram indicated that each Vote should be associated with a User.

- The functional requirements specified that voting could be anonymous, meaning a userId would not always be available.

To resolve this dichotomy, the userId attribute within the Vote entity was implemented as an optional (nullable) field.

This solution provides the necessary flexibility:

- For public polls, where anonymity is allowed, votes can be submitted without a userId. The application logic permits the userId field to be null in these cases.

- For private polls, which require user authentication, the application's business logic enforces that the userId field must be populated. Any vote submission to a private poll without a valid userId is rejected.

### Pending issues

In my opinion there is nothing glaringly obvious missing in this implementation, based on the requirements we were give. I did however not do the optional tasks, only the mandatory ones. Also I could probably have used Json serializing more, aswell as made my swagger ui better.

Also, right now I dont have any actual authentication to check if a user is the user they say they are. All you need to do is include the userId you want to usein the API requests. This is obviously not good and a major security risk. But something I can do in the future is to for example use Bearer tokens as an authentication mechanism, but for now this is missing.

---

## Additional information (not necessarily relevant (NixOS propaganda))

### System-Wide Dependencies

Core development tools were installed system-wide by adding them to the NixOS `configuration.nix` file. This method ensures that essential tools are always available in the global environment. The declarative nature of NixOS means that the system's state is explicitly defined in configuration files, making it trivial to replicate or recover. 

The relevant section of my `/etc/nixos/configuration.nix` is as follows:

```nix
# /etc/nixos/configuration.nix

environment.systemPackages = with pkgs; [
  # Version Control
  git
  lazygit # A terminal UI for git

  # Core Development Runtimes & SDKs
  nodejs_20
  jdk21
  gradle

  # Development Environment & Tools
  vscode-fhs # Visual Studio Code with FHS compliance for broader tool compatibility
  bruno      # API client for testing
];

---

### Project-Specific Environment with Nix Flakes

For project-specific dependencies, **Nix Flakes** were used to create an isolated and perfectly reproducible environment. A `flake.nix` file at the root of the project directory defines all inputs (e.g., specific versions of libraries, compilers, or tools) required to build and run the code.

The key advantages of this approach are:

* **Reproducibility**: Anyone with Nix installed can enter the exact same development environment by running `nix develop`, completely eliminating "it works on my machine" issues.
* **Isolation**: Project dependencies do not pollute the global system or conflict with other projects.
* **Declarative Management**: The entire development environment is explicitly defined in a single file, which can be version-controlled with `git`.

Here is a simplified example of a `flake.nix` for this project:

```nix
# flake.nix

{
  description = "Development environment for DAT250 Experiment 2";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
  };

  outputs = { self, nixpkgs }:
    let
      pkgs = nixpkgs.legacyPackages.x86_64-linux;
    in
    {
      devShells.x86_64-linux.default = pkgs.mkShell {
        # Define the packages available inside the shell
        buildInputs = with pkgs; [
          nodejs_20
          jdk21
          gradle
        ];
      };
    };
}

---

### Verification and Conclusion

The installation was successful, with no significant issues encountered. All tools were verified by checking their versions from the command line (e.g. `java --version`).

The use of NixOS and Nix Flakes provides a robust foundation for development. This declarative and reproducible setup is highly beneficial for academic and professional work, as it guarantees a consistent environment across different machines and over time.