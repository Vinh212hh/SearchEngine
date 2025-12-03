# Product Search & Comparison App (Java Swing)

## Overview

This is a desktop product search assistant built with **Java Swing**.  
It allows users to:

- Log in / register with their own account  
- Search for a product keyword (Vietnamese or English)  
- Open **real search pages** on Vietnamese e-commerce sites (Shopee, Tiki, Lazada, etc.)  
- Filter results by website  
- Compare simulated prices between shops  
- Save favorite search links and manage a list of saved websites  
- See **Google-style keyword suggestions** while typing

The goal is to simulate a real-world “shopping search tool” that helps users quickly jump to relevant product pages on multiple platforms.

---

## Features

### 1. User Account Management

- Register new account (username + password)
- Log in with saved accounts
- “Remember account” option (auto-fill last username)
- Forgot password (show stored password for a username)
- Delete current account
- Delete all accounts stored on this machine (for sharing the app with others)

### 2. Keyword Search & Suggestions

- Search bar for product name / keyword
- Live keyword suggestions (Google Suggest + local history)
- Keyword history stored in a local file (`.search_keywords.txt`)

### 3. Product Search (No API, Real Links)

No search API is used.  
Instead, the app generates **real search URLs** for each supported e-commerce site:

- Shopee  
- Lazada  
- Tiki  
- Sendo  
- Thế Giới Di Động  
- Điện Máy Xanh  
- FPT Shop  
- CellphoneS  
- Hoàng Hà Mobile  
- Phong Vũ  
- Nguyễn Kim  
- An Phát  
- GearVN  
- Hanoicomputer  
- Meta.vn  

For each site, the app creates a `SearchResult` with:

- Site name (Shopee, Tiki, …)  
- Title: `Search "<keyword>" on <site>`  
- URL: the real search link for that keyword on that site  
- A **simulated price** used only for comparison

Double-clicking a result opens the link in the default web browser.

### 4. Filtering & Comparison

- **Filter by website** using the list on the left panel
- **Compare products**:
  - Filter by price range
  - Show cheapest product
  - Show most expensive product
  - Compare two websites (which one is cheaper and by how much)

### 5. Saved Websites (Bookmarks)

- Right-click a result → “Save this website”
- Saved websites are stored in `.saved_webs.txt`
- “Saved websites” dialog:
  - Double-click to open
  - Delete selected entries

### 6. Navigation

- “Back to login” button in the main window
- Account menu:
  - Logout
  - Delete current account

---

## Technology Stack

- **Language:** Java (JDK 17+ recommended)  
- **GUI:** Swing  
- **Architecture:** Desktop monolithic app with separate classes for:
  - UI (frames & dialogs)
  - User & file management
  - Search & suggestion service
- **Data storage:**
  - Plain text files in the user home directory:
    - `.users.txt` – accounts (username:password)
    - `.last_user.txt` – last remembered username
    - `.search_keywords.txt` – search keyword history
    - `.saved_webs.txt` – saved websites
- **Libraries:**
  - `org.json` (e.g. `json-20231013.jar`) for parsing Google Suggest responses

---

## How It Works

1. User logs in or creates a new account.  
2. User types a product keyword and sees Google-style suggestions.  
3. On search:
   - The app generates search URLs for all supported e-commerce sites.
   - Results are shown in the right panel (one row per site).
   - Sites are listed in the left panel for filtering.
4. User can:
   - Double-click a result to open the real search page in the browser
   - Save a result as a bookmark (right-click)
   - Open the bookmarks list and manage saved websites
   - Open the comparison dialog to filter and compare simulated prices

---

## How to Run

### Requirements

- JDK 17 or higher  
- A Java IDE (IntelliJ IDEA recommended)  
- `json-20231013.jar` (or similar `org.json`)

### Steps (IntelliJ IDEA)

1. Create a new **Java** project.
2. Add `SearchNewsApp.java` (full code in this repo) to the `src` folder.
3. Add the JSON library:
   - `File → Project Structure → Modules → Dependencies → + (JAR) → select json-20231013.jar`
4. Build the project.
5. Run the `main` method in `SearchNewsApp`.

The login window should appear.  
You can then **register a new account** and start searching and comparing products.

MY DEMO VIDEO: https://www.youtube.com/watch?v=f40DGk2P5Uw
