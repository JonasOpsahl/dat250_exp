import { useState, useEffect } from "react";
import styles from "./App.module.css";
import CreatePoll from "./components/CreatePoll";
import VoteOnPoll from "./components/VoteOnPoll";
import Login from "./components/Login";
import { useAuth } from "./Auth";
import { fetchVisiblePolls, type Poll } from "./api";

function App() {
  const { currentUser, logout } = useAuth();
  const [activeTab, setActiveTab] = useState<"create" | "vote">("vote");
  const [polls, setPolls] = useState<Poll[]>([]);

  const [isLoginView, setIsLoginView] = useState(false);

  const refreshPolls = () => {
    fetchVisiblePolls(currentUser?.id)
      .then(setPolls)
      .catch((err) => {
        console.error("Failed to load polls:", err);
      });
  };

  useEffect(() => {
    refreshPolls();
    if (!currentUser) {
      setIsLoginView(false);
      setActiveTab("vote");
    }
  }, [currentUser]);

  const handlePollCreated = () => {
    refreshPolls();
    setActiveTab("vote");
  };

  if (isLoginView) {
    return <Login onLoginSuccess={() => setIsLoginView(false)} />;
  }

  return (
    <div className={styles.appContainer}>
      <header className={styles.header}>
        {currentUser ? (
          <>
            <span>
              Welcome, <strong>{currentUser.username}</strong>! (userID:{" "}
              {currentUser.id})
            </span>
            <button onClick={logout} className={styles.logoutButton}>
              Logout
            </button>
          </>
        ) : (
          <>
            <span>Welcome, Guest!</span>
            <button
              onClick={() => setIsLoginView(true)}
              className={styles.submitButton}
            >
              Login / Register
            </button>
          </>
        )}
      </header>

      <nav className={styles.tabContainer}>
        {currentUser && (
          <button
            className={`${styles.tabButton} ${
              activeTab === "create" ? styles.tabButtonActive : ""
            }`}
            onClick={() => setActiveTab("create")}
          >
            Create Poll
          </button>
        )}
        <button
          className={`${styles.tabButton} ${
            activeTab === "vote" ? styles.tabButtonActive : ""
          }`}
          onClick={() => setActiveTab("vote")}
        >
          Vote on Polls
        </button>
      </nav>

      <main className={styles.tabContent}>
        {activeTab === "create" && currentUser && (
          <CreatePoll onPollCreated={handlePollCreated} />
        )}

        {activeTab === "vote" && (
          <div>
            <h2>Available Polls</h2>
            {polls.length > 0 ? (
              <div className={styles.pollListContainer}>
                {polls.map((poll) => (
                  <VoteOnPoll key={poll.pollId} pollData={poll} />
                ))}
              </div>
            ) : (
              <p>No polls to vote on right now</p>
            )}
          </div>
        )}
      </main>
    </div>
  );
}

export default App;
