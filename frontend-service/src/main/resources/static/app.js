/**
 * HeaderRender class extends HTMLElement to create a custom header component
 */
class User extends HTMLElement {
    constructor() {
        super(...arguments);
        this.username = "inconnu";
    }
    /**
     * Called when the custom element is connected to the DOM
     * Initializes the component by getting username attribute, rendering content, and setting up event listeners
     * @returns {void}
     */
    connectedCallback() {
        var _a;
        this.username = this.getAttribute("username") || "inconnu";
        this.render();
        (_a = this.querySelector("#header-user")) === null || _a === void 0 ? void 0 : _a.addEventListener("click", this.toggleUserMenu.bind(this));
    }
    /**
     * Called when the custom element is disconnected from the DOM
     * Cleans up event listeners to prevent memory leaks
     * @returns {void}
     */
    disconnectedCallback() {
        var _a;
        (_a = this.querySelector("#header-user")) === null || _a === void 0 ? void 0 : _a.removeEventListener("click", this.toggleUserMenu.bind(this));
    }
    /**
     * Toggles the visibility of the user menu dropdown
     * @param {Event} event - The click event that triggered the toggle
     * @returns {void}
     */
    toggleUserMenu(event) {
        event.stopPropagation();
        let menu = this.querySelector(".header-user-menu");
        if (menu)
            this.hideMenu();
        else
            this.createUserMenu();
    }
    /**
     * Creates and displays the user dropdown menu with profile information and menu items
     * @returns {void}
     */
    createUserMenu() {
        var _a;
        const menu = document.createElement("div");
        menu.className = "header-user-menu";
        (_a = this.querySelector("#header-user")) === null || _a === void 0 ? void 0 : _a.appendChild(menu);
        const profile = `<div class="header-user-profile">` +
            `<img class="header-user-avatar" src="/img/avatar.webp" alt="avatar">` +
            `<div class="header-user-name">` +
            `<span>${this.username}</span>` +
            `</div>` +
            `</div>`;
        menu.appendChild(new DOMParser().parseFromString(profile, "text/html").body.firstChild);
        const menuItems = [
            { text: "Profile", action: () => this.handleClick("profile") },
            { text: "Déconnexion", action: () => this.handleClick("logout") },
        ];
        menuItems.forEach((item) => {
            const menuItem = document.createElement("div");
            menuItem.className = "header-user-item";
            menuItem.textContent = item.text;
            menuItem.addEventListener("click", item.action);
            menu.appendChild(menuItem);
        });
        requestAnimationFrame(() => menu.classList.add("show"));
        this.clickListener = (event) => {
            if (!menu.contains(event.target))
                this.hideMenu();
        };
        document.addEventListener("click", this.clickListener, { once: true });
    }
    /**
     * Hides the user menu dropdown with animation and cleans up event listeners
     * @returns {void}
     */
    hideMenu() {
        const menu = this.querySelector(".header-user-menu");
        if (menu) {
            menu.classList.remove("show");
            menu.classList.add("hide");
            setTimeout(() => menu.remove(), 300);
        }
        if (this.clickListener) {
            document.removeEventListener("click", this.clickListener);
            this.clickListener = undefined;
        }
    }
    /**
     * Handles click events on menu items and performs corresponding actions
     * @param {String} action - The action identifier for the clicked menu item
     * @returns {void}
     */
    handleClick(action) {
        switch (action) {
            case "profile":
                window.location.href = "/profile";
                break;
            case "logout":
                window.location.href = "/login";
                break;
            // default:
            //     console.log("Unknown action");
        }
        this.hideMenu();
    }
    /**
     * Renders the header component HTML structure with logo and user icon
     * @returns {void}
     */
    render() {
        this.innerHTML =
            `<div id="header-user" class="header-user">` +
                `<svg xmlns="http://www.w3.org/2000/svg" width="28" height="32" viewBox="0 0 448 512"><path fill="currentColor" d="M224 256a128 128 0 1 0 0-256a128 128 0 1 0 0 256m-96 55.2C54 332.9 0 401.3 0 482.3C0 498.7 13.3 512 29.7 512h388.6c16.4 0 29.7-13.3 29.7-29.7c0-81-54-149.4-128-171.1V362c27.6 7.1 48 32.2 48 62v40c0 8.8-7.2 16-16 16h-16c-8.8 0-16-7.2-16-16s7.2-16 16-16v-24c0-17.7-14.3-32-32-32s-32 14.3-32 32v24c8.8 0 16 7.2 16 16s-7.2 16-16 16h-16c-8.8 0-16-7.2-16-16v-40c0-29.8 20.4-54.9 48-62v-57.1q-9-.9-18.3-.9h-91.4q-9.3 0-18.3.9v65.4c23.1 6.9 40 28.3 40 53.7c0 30.9-25.1 56-56 56s-56-25.1-56-56c0-25.4 16.9-46.8 40-53.7zM144 448a24 24 0 1 0 0-48a24 24 0 1 0 0 48"/></svg>` +
                `</div>`;
    }
}

class Sort extends HTMLElement {
    constructor() {
        super(...arguments);
        this.sortByField = null;
        this.sort = "asc";
        this.name = null;
        this.down = `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 448 512"><path fill="currentColor" d="M201.4 374.6c12.5 12.5 32.8 12.5 45.3 0l160-160c12.5-12.5 12.5-32.8 0-45.3s-32.8-12.5-45.3 0L224 306.7L86.6 169.4c-12.5-12.5-32.8-12.5-45.3 0s-12.5 32.8 0 45.3l160 160z"/></svg>`;
        this.up = `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 448 512"><path fill="currentColor" d="M201.4 137.4c12.5-12.5 32.8-12.5 45.3 0l160 160c12.5 12.5 12.5 32.8 0 45.3s-32.8 12.5-45.3 0L224 205.3L86.6 342.6c-12.5 12.5-32.8 12.5-45.3 0s-12.5-32.8 0-45.3l160-160z"/></svg>`;
        this.handleClick = () => {
            const params = new URLSearchParams(window.location.search);
            const currentSort = params.get("sort") || "asc";
            this.sort = currentSort === "asc" ? "desc" : "asc";
            params.set("sort", this.sort);
            params.set("sortBy", this.sortByField || "lastName");
            params.set("page", "1");
            window.location.search = params.toString();
        };
    }
    connectedCallback() {
        this.sortByField = this.getAttribute("sortby");
        this.name = this.getAttribute("name");
        if (!this.sortByField || !this.name)
            return;
        this.addListeners();
        this.render();
    }
    disconnectedCallback() {
        this.removeListeners();
    }
    addListeners() {
        this.addEventListener("click", this.handleClick);
        const params = new URLSearchParams(window.location.search);
        this.sort = params.get("sort") === "desc" ? "desc" : "asc";
    }
    removeListeners() {
        this.removeEventListener("click", this.handleClick);
    }
    render() {
        this.innerHTML = `${this.name}<span>${this.sort === "asc" ? this.down : this.up}</span>`;
    }
}

/******************************************************************************
Copyright (c) Microsoft Corporation.

Permission to use, copy, modify, and/or distribute this software for any
purpose with or without fee is hereby granted.

THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH
REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY
AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT,
INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM
LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR
OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
PERFORMANCE OF THIS SOFTWARE.
***************************************************************************** */
/* global Reflect, Promise, SuppressedError, Symbol, Iterator */


function __awaiter(thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
}

typeof SuppressedError === "function" ? SuppressedError : function (error, suppressed, message) {
    var e = new Error(message);
    return e.name = "SuppressedError", e.error = error, e.suppressed = suppressed, e;
};

class Search extends HTMLElement {
    constructor() {
        super();
        this.searchLock = false;
        this.boundHandleKeydown = this.handleKeydown.bind(this);
        this.boundToggleSearchForm = this.toggleSearchForm.bind(this);
        this.boundHandleOutsideClick = this.handleOutsideClick.bind(this);
    }
    connectedCallback() {
        this.render();
        this.addListeners();
    }
    disconnectedCallback() {
        this.removeListeners();
        if (this.debounceTimer) {
            window.clearTimeout(this.debounceTimer);
            this.debounceTimer = undefined;
        }
    }
    addListeners() {
        var _a;
        document.addEventListener("keydown", this.boundHandleKeydown);
        (_a = this.querySelector("#header-search")) === null || _a === void 0 ? void 0 : _a.addEventListener("click", this.boundToggleSearchForm);
    }
    removeListeners() {
        var _a;
        document.removeEventListener("keydown", this.boundHandleKeydown);
        (_a = this.querySelector("#header-search")) === null || _a === void 0 ? void 0 : _a.removeEventListener("click", this.boundToggleSearchForm);
    }
    handleKeydown(event) {
        if ((event.ctrlKey || event.metaKey) && event.key === "k") {
            event.preventDefault();
            this.toggleSearchForm();
        }
    }
    toggleSearchForm() {
        const search = document.body.querySelector(".search-container");
        if (search) {
            this.hideSearchForm(search);
            document.removeEventListener("mousedown", this.boundHandleOutsideClick);
        }
        else {
            this.createSearchForm();
            document.addEventListener("mousedown", this.boundHandleOutsideClick);
        }
    }
    hideSearchForm(search) {
        const searchInput = search.querySelector("#search-input");
        if (searchInput && this.inputListener) {
            searchInput.removeEventListener("input", this.inputListener);
        }
        const anim = search.animate([{ opacity: 1, transform: "translateY(0)" }, {
                opacity: 0,
                transform: "translateY(-200px)"
            },], {
            duration: 300, easing: "ease-in-out", fill: "forwards",
        });
        anim.onfinish = () => {
            search.remove();
            document.removeEventListener("mousedown", this.boundHandleOutsideClick);
        };
    }
    createSearchForm() {
        const form = document.createElement("div");
        form.className = "search-container";
        form.innerHTML =
            `<div class="search-form">` +
                `<input id="search-input" type="text" placeholder="Rechercher un patient ..." class="search-input" aria-label="Recherche patient" role="searchbox">` +
                `<span id="clear-form" title="Effacer la recherche">` +
                `<svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 384 512"><path fill="currentColor" d="M342.6 150.6c12.5-12.5 12.5-32.8 0-45.3s-32.8-12.5-45.3 0L192 210.7L86.6 105.4c-12.5-12.5-32.8-12.5-45.3 0s-12.5 32.8 0 45.3L146.7 256L41.4 361.4c-12.5 12.5-12.5 32.8 0 45.3s32.8 12.5 45.3 0L192 301.3l105.4 105.3c12.5 12.5 32.8 12.5 45.3 0s12.5-32.8 0-45.3L237.3 256z"/></svg>` +
                `</span>` +
                `</div>` +
                `<ul id="search-results" class="search-results" role="listbox" aria-label="Résultats de recherche"></ul>`;
        document.body.appendChild(form);
        this.clearButton(form);
        this.searchListener(form);
        const searchInput = form.querySelector("#search-input");
        searchInput.addEventListener("keydown", this.handleNavigation.bind(this, form));
        requestAnimationFrame(() => {
            form.classList.add("show");
            searchInput.focus();
        });
    }
    searchListener(form) {
        const searchInput = form.querySelector("#search-input");
        if (searchInput) {
            this.inputListener = (event) => {
                const target = event.target;
                const value = target.value.trim();
                const resultsContainer = form.querySelector("#search-results");
                if (this.searchLock)
                    return;
                if (resultsContainer) {
                    this.searchLock = true;
                    const anim = resultsContainer.animate([{ opacity: 1, transform: "translateY(0)" }, {
                            opacity: 0,
                            transform: "translateY(-20px)"
                        },], {
                        duration: 300, easing: "ease-in-out", fill: "forwards",
                    });
                    anim.onfinish = () => {
                        this.searchLock = false;
                        resultsContainer.innerHTML = "";
                    };
                }
                if (this.debounceTimer)
                    window.clearTimeout(this.debounceTimer);
                this.debounceTimer = window.setTimeout(() => !this.searchLock && value.length > 0 && this.searchPatients(value), 1000);
            };
            searchInput.addEventListener("input", this.inputListener);
        }
    }
    // @ts-ignore
    searchPatients(value) {
        return __awaiter(this, void 0, void 0, function* () {
            try {
                const response = yield fetch(`/api/patients/search?q=${encodeURIComponent(value)}`);
                if (!response.ok)
                    throw new Error(`Erreur HTTP: ${response.status}`);
                const results = yield response.json();
                const resultsContainer = document.body.querySelector("#search-results");
                if (resultsContainer) {
                    resultsContainer.innerHTML = "";
                    results.data.forEach((patient) => {
                        const item = document.createElement("li");
                        item.className = "search-item";
                        item.textContent = `${patient.lastName} ${patient.firstName}`;
                        item.setAttribute("role", "option");
                        item.tabIndex = -1;
                        item.addEventListener("click", () => {
                            window.location.href = `/patient/${patient.id}`;
                        });
                        resultsContainer.appendChild(item);
                    });
                    const first = resultsContainer.querySelector("li");
                    if (first)
                        first.classList.add("selected");
                    resultsContainer.animate([{ opacity: 0, transform: "translateY(-20px)" }, {
                            opacity: 1,
                            transform: "translateY(0)"
                        },], {
                        duration: 300, easing: "ease-in-out", fill: "forwards",
                    });
                }
            }
            catch (error) {
                console.error("Erreur lors de la recherche:", error);
            }
        });
    }
    handleNavigation(form, event) {
        const results = form.querySelectorAll("#search-results li");
        if (!results.length)
            return;
        // @ts-ignore
        let idx = Array.from(results).findIndex((li) => li.classList.contains("selected"));
        switch (event.key) {
            case "ArrowDown":
                event.preventDefault();
                idx = idx < results.length - 1 ? idx + 1 : 0;
                break;
            case "ArrowUp":
                event.preventDefault();
                idx = idx > 0 ? idx - 1 : results.length - 1;
                break;
            case "Enter":
                if (idx >= 0) {
                    event.preventDefault();
                    results[idx].click();
                }
                return;
            default:
                return;
        }
        results.forEach((li) => li.classList.remove("selected"));
        const current = results[idx];
        current.classList.add("selected");
        current.scrollIntoView({ block: "nearest" });
    }
    clearButton(form) {
        const clearButton = form.querySelector("#clear-form");
        const searchInput = form.querySelector("#search-input");
        if (clearButton && searchInput) {
            clearButton.addEventListener("click", () => {
                searchInput.value = "";
                searchInput.focus();
                const resultsContainer = form.querySelector("#search-results");
                if (resultsContainer)
                    resultsContainer.innerHTML = "";
            });
        }
    }
    handleOutsideClick(event) {
        const search = document.body.querySelector(".search-container");
        const header = this.querySelector("#header-search");
        if (search && !search.contains(event.target) && header && !header.contains(event.target)) {
            this.hideSearchForm(search);
        }
    }
    render() {
        this.innerHTML =
            `<div id="header-search" class="header-search" tabindex="0" aria-label="Ouvrir la recherche">` +
                `<svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 512 512"><path fill="currentColor" d="M416 208c0 45.9-14.9 88.3-40 122.7l126.6 126.7c12.5 12.5 12.5 32.8 0 45.3s-32.8 12.5-45.3 0L330.7 376c-34.4 25.2-76.8 40-122.7 40C93.1 416 0 322.9 0 208S93.1 0 208 0s208 93.1 208 208M208 352a144 144 0 1 0 0-288a144 144 0 1 0 0 288"/></svg>` +
                `</div>`;
    }
}

class Pagination extends HTMLElement {
    constructor() {
        super(...arguments);
        this.totalElements = null;
        this.currentPage = null;
        this.totalPages = null;
        this.pageSize = null;
        this.onPrevClick = () => {
            if (this.currentPage !== null) {
                const params = new URLSearchParams(window.location.search);
                params.set("page", String(this.currentPage - 1));
                window.location.search = params.toString();
            }
        };
        this.onNextClick = () => {
            if (this.currentPage !== null) {
                const params = new URLSearchParams(window.location.search);
                params.set("page", String(this.currentPage + 1));
                window.location.search = params.toString();
            }
        };
        this.onPageSizeChange = (event) => {
            const selectElement = event.target;
            const newPageSize = parseInt(selectElement.value, 10);
            if (this.pageSize !== null && newPageSize !== this.pageSize) {
                const params = new URLSearchParams(window.location.search);
                params.set("size", String(newPageSize));
                window.location.search = params.toString();
            }
        };
    }
    connectedCallback() {
        const totalElements = this.getAttribute("total-elements");
        this.totalElements = totalElements ? parseInt(totalElements, 10) : null;
        const currentPage = this.getAttribute("current-page");
        this.currentPage = currentPage ? parseInt(currentPage, 10) : null;
        const totalPages = this.getAttribute("total-pages");
        this.totalPages = totalPages ? parseInt(totalPages, 10) : null;
        const pageSize = this.getAttribute("page-size");
        this.pageSize = pageSize ? parseInt(pageSize, 10) : null;
        this.render();
        this.addListeners();
    }
    disconnectedCallback() {
        this.removeListeners();
    }
    addListeners() {
        var _a, _b, _c;
        (_a = this.querySelector("#prev-button")) === null || _a === void 0 ? void 0 : _a.addEventListener("click", this.onPrevClick);
        (_b = this.querySelector("#next-button")) === null || _b === void 0 ? void 0 : _b.addEventListener("click", this.onNextClick);
        (_c = this.querySelector("#page-size")) === null || _c === void 0 ? void 0 : _c.addEventListener("change", this.onPageSizeChange);
    }
    removeListeners() {
        var _a, _b, _c;
        (_a = this.querySelector("#prev-button")) === null || _a === void 0 ? void 0 : _a.removeEventListener("click", this.onPrevClick);
        (_b = this.querySelector("#next-button")) === null || _b === void 0 ? void 0 : _b.removeEventListener("click", this.onNextClick);
        (_c = this.querySelector("#page-size")) === null || _c === void 0 ? void 0 : _c.removeEventListener("change", this.onPageSizeChange);
    }
    render() {
        const disabledCurrentPage = this.currentPage === 1 ? "disabled" : "";
        const disabledNextPage = this.currentPage === this.totalPages ? "disabled" : "";
        const pageSizeFive = this.pageSize === 5 ? "selected" : "";
        const pageSizeTen = this.pageSize === 10 ? "selected" : "";
        const pageSizeTwenty = this.pageSize === 20 ? "selected" : "";
        const pageSizeFifty = this.pageSize === 50 ? "selected" : "";
        const currentPageDisplay = this.totalElements !== null &&
            this.pageSize !== null &&
            this.currentPage !== null
            ? Math.min(this.currentPage * this.pageSize, this.totalElements)
            : "-";
        const totalPagesDisplay = this.totalPages !== null && this.pageSize !== null
            ? this.totalPages * this.pageSize
            : "-";
        this.innerHTML =
            `<div class="page-size">` +
                `<span>Éléments par page</span>` +
                `<select id="page-size">` +
                `<option value="5" ${pageSizeFive}>5</option>` +
                `<option value="10" ${pageSizeTen}>10</option>` +
                `<option value="20" ${pageSizeTwenty}>20</option>` +
                `<option value="50" ${pageSizeFifty}>50</option>` +
                `</select>` +
                `</div>` +
                `<div class="pagination">` +
                `<div>${currentPageDisplay} <span>sur ${totalPagesDisplay}</span></div>` +
                `<div class="group-btn">` +
                `<button id="prev-button" ${disabledCurrentPage}><svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 512 512"><path fill="currentColor" d="M512 256a256 256 0 1 0-512 0a256 256 0 1 0 512 0M271 135c9.4-9.4 24.6-9.4 33.9 0s9.4 24.6 0 33.9l-87 87l87 87c9.4 9.4 9.4 24.6 0 33.9s-24.6 9.4-33.9 0L167 273c-9.4-9.4-9.4-24.6 0-33.9z"/></svg></button>` +
                `<button id="next-button" ${disabledNextPage}><svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 512 512"><path fill="currentColor" d="M0 256a256 256 0 1 0 512 0a256 256 0 1 0-512 0m241 121c-9.4 9.4-24.6 9.4-33.9 0s-9.4-24.6 0-33.9l87-87l-87-87c-9.4-9.4-9.4-24.6 0-33.9s24.6-9.4 33.9 0L345 239c9.4 9.4 9.4 24.6 0 33.9z"/></svg></button>` +
                `</div>` +
                `</div>`;
    }
}

customElements.define("user-component", User);
customElements.define("sort-component", Sort);
customElements.define("search-component", Search);
customElements.define("pagination-component", Pagination);
