# Report: Experiment 3 - DAT250

I was not sure what I was gonna call the document since it is experiment 3, but the hand in description says to call it dat250-expass6.md. And I didnt know if I was supposed to upload this file with the github repo or if I was supposed to put this singular file in the repo with the other experiment documentation and then just link it to this repo. I ended up putting this document in the same repo as the other report and then making the code repository public for you to see.

This document outlines the technical challenges and lists the pending issues for the Poll Application project.

---

## Technical Problems Encountered

The development process involved a few technical hurdles, ranging from environment configuration to application logic. The primary challenge was my limited prior experience with JavaScript and its ecosystem, which made the learning curve steeper but ultimately proved to be a valuable learning experience.

### 1. Environment and Build Configuration

- **Incorrect Build Path:** The initial project setup included a script to automatically build the frontend and copy it to the backend directory. This script failed because it was configured to look for a directory named `backend`, whereas my project's backend directory was named `api`. Correcting the path in the build script resolved this issue.
- **Concurrent Server Execution:** Manually starting the frontend and backend servers for each development session was inefficient. To solve this, I configured the Vite development server to proxy API requests to the backend.

  - First, I installed `concurrently` to run multiple commands at once: `npm install concurrently --save-dev`.
  - Next, I modified the `vite.config.ts` file to proxy requests from `/api` to my backend server running on port `8080`:

    ```typescript
    import { defineConfig } from "vite";
    import react from "@vitejs/plugin-react";

    export default defineConfig({
      plugins: [react()],
      server: {
        proxy: {
          "/api": {
            target: "http://localhost:8080",
            changeOrigin: true,
          },
        },
      },
    });
    ```

  - Finally, I added a `start` script to the `package.json` file to launch both servers with a single command:
    `json
    "scripts": {
        "start": "concurrently \"npm run dev\" \"cd ../api && ./gradlew bootRun\""
    },
    `
    Now, `npm run start` successfully initiates both the frontend and backend environments.

### 2. Application Logic and Bugs

- **Non-functional Voting System:** A major bug prevented any user from voting on polls. This was traced back to a stray `isDisabled` variable in the frontend code. This variable was a remnant from an earlier experiment to display private polls as non-interactive elements. After I decided against that approach, I forgot to remove the variable, which inadvertently disabled voting across the entire application. Simply removing it fixed the issue.
- **Private Poll Visibility:** The creator of a private poll and invited users were unable to view it. My backend was initially designed to only serve public polls by default, and then needed the pollID to view private polls. My first thought was to return all polls and filter on the frontend, but I dismissed this due to potential security risks. The final solution involved modifying the backend `getPolls` function to accept an optional `userID`. If a `userID` is provided, the function now returns all public polls plus any private polls the user owns or is invited to.
- **Forgotten Anonymous Voting Requirement:** Near the end of the project, I realized I had overlooked the requirement for anonymous voting. My initial implementation forced users to a login/register page upon visiting the site. I had to refactor the user flow to make the main poll-viewing page accessible to everyone and make logging in an optional action via a dedicated button.

---

## Link to Code Repository

The code for the project can be found here, it is not entirely complete as the next part of the report will show. But it is a working solution.

I had a problem with the original git repository where I had forgot to add the ./gradle and those kinds of files to the .gitignore. Which led to that file getting stuck somewhere and I kept getting an error saying the file was too large to push. So I created a new repository which you can find here and everything should still be working:

[Link to github repository](https://github.com/JonasOpsahl/dat250_exp3)

---

## Pending Issues

While the core functionality is in place, there are several known issues and areas for future improvement that were not addressed both due to time constraints and also I didn't know how big the scope of the task was supposed to be. As I already spent a very long time on the current product.

- **User session persistence:** The application currently lacks session management. If a logged-in user refreshes the page, their authentication state is lost, and they are required to log in again. Implementing token-based authentication (e.g., JWT) stored in local storage or cookies would provide a better user experience.
- **Bad to no user validation:** The user creation process is not robust. It's currently possible to create multiple user accounts that point to the same underlying user entity simply by providing an existing username. There is no check for unique usernames upon registration.
- **Security vulnerabilities:** User authentication is rudimentary and insecure. Passwords are not hashed or encrypted, and there is minimal input validation. This is a critical issue that needs to be addressed by implementing password hashing and more thorough server-side validation.
- **Incomplete Frontend Feature Set:** The backend API supports full CRUD operations for most models. However, the frontend currently only implements a subset of this functionality, such as creating polls, creating users, and voting. Features like updating or deleting a poll are not yet available in the user interface.
- **UI/UX Flow Limitation:** On the main page, if a logged-out user or guest clicks the "Login/Register" button, they are taken to a new view. From this view, there is no button or link to navigate back to the poll list. The user must manually refresh the page or complete the login/registration process to exit.
