# LaxBench

LaxBench is a Kotlin Multiplatform app for lacrosse bench personnel — the scorekeeper/timekeeper
running the table during a game. It tracks the score, penalty clocks, and game events live, and
exports a filled-in score sheet as a PDF when the game is done. It targets Android, iOS, Desktop
(JVM), and Web (Wasm/JS).

The app is written entirely by an agentic coding workflow.

## Features

- **Team setup** — enter each team's name and color before the game starts.
- **Live score** — tap either team's score to record a goal, with scorer and optional assist
  player numbers.
- **Game clock** — start, stop, and resume a running game timer; the elapsed time is used to
  timestamp every event recorded during the game.
- **Fouls and penalty timers** — record a foul against a player as minor, major, or an expulsion.
  - Minor fouls (e.g. holding, offside, interference) run a 30 second timer.
  - Major fouls (e.g. cross-check, slashing, unnecessary roughness) carry a selectable penalty
    duration.
  - Expulsions run a 5 minute timer.
  - A "Current fouls" view lists every player currently serving a penalty with their combined
    remaining time, and lets you release a player early or cancel one of several stacked
    penalties.
  - Penalty timers automatically pause when the game clock is stopped and resume when it restarts,
    and a pop-up notifies the bench when a player is released.
- **Saves and face-offs** — quick one-tap logging of goalie saves and face-off wins per team.
- **Time-outs** — recording a time-out (from the "Stop all clocks" action) starts a visible 90
  second time-out countdown that blinks once expired.
- **Manage game** — a full editing view to review, add, edit, or delete any recorded goal, foul,
  face-off, time-out, or save, and to correct a team's name or color after the game has started.
- **PDF score sheet export** — generates a complete score sheet PDF from the game's goals, fouls,
  saves, face-offs, and time-outs, ready to save or share.

