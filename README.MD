# Tausch

*Quick and secure item trading between players.*

---

## Description

Provides a robust, intuitive, and secure system for players to trade items with confirmation, avoiding exploits, with logs stored in MongoDB for auditing purposes.

---

## Main Features

- Player trading via `/trade <player>`
- Two-step confirmation to prevent mistakes and duplication
- Ignore system to block trade requests
- Full logs stored in MongoDB for auditing
- Intuitive menus and configurable messages in multiple languages
- Admin commands to reload configs and view trade history
- Optimized for high performance on large servers
- Flexible and scalable system designed for future addons

---

## Commands

| Command                   | Usage                             | Permissions     | Description                                               |
|---------------------------|-----------------------------------|------------------|-----------------------------------------------------------|
| `/trade <player>`         | Send trade request                | `-`              | Sends a trade request to the specified player.            |
| `/trade accept`           | Accept pending request            | `-`              | Accepts the received trade request.                       |
| `/trade decline`          | Decline pending request           | `-`              | Declines the received trade request.                      |
| `/trade ignore`           | Toggle trade request ignoring     | `-`              | Toggles whether you receive trade requests.               |
| `/tausch`                 | Show plugin information           | `-`              | Displays version, author, and plugin details.             |
| `/tausch reload`          | Reload config and messages        | `tausch.admin`   | Reloads configuration and message files.                  |
| `/tradehistory <player>`  | Show trade history                | `tausch.admin`   | Opens a menu with the trade history of the specified player. |
