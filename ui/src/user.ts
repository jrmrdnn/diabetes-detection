/**
 * HeaderRender class extends HTMLElement to create a custom header component
 */
export class User extends HTMLElement {
    private clickListener?: (event: Event) => void;
    private username: string = "inconnu"

    /**
     * Called when the custom element is connected to the DOM
     * Initializes the component by getting username attribute, rendering content, and setting up event listeners
     * @returns {void}
     */
    connectedCallback(): void {
        this.username = this.getAttribute("username") || "inconnu";
        this.render();
        this.querySelector("#header-user")?.addEventListener(
            "click",
            this.toggleUserMenu.bind(this)
        );
    }

    /**
     * Called when the custom element is disconnected from the DOM
     * Cleans up event listeners to prevent memory leaks
     * @returns {void}
     */
    disconnectedCallback(): void {
        this.querySelector("#header-user")?.removeEventListener(
            "click",
            this.toggleUserMenu.bind(this)
        );
    }

    /**
     * Toggles the visibility of the user menu dropdown
     * @param {Event} event - The click event that triggered the toggle
     * @returns {void}
     */
    toggleUserMenu(event: Event): void {
        event.stopPropagation();
        let menu: Element | null = this.querySelector(".header-user-menu");
        if (menu) this.hideMenu();
        else this.createUserMenu();
    }

    /**
     * Creates and displays the user dropdown menu with profile information and menu items
     * @returns {void}
     */
    createUserMenu(): void {
        const menu = document.createElement("div");
        menu.className = "header-user-menu";
        this.querySelector("#header-user")?.appendChild(menu);

        const profile =
            `<div class="header-user-profile">` +
            `<img class="header-user-avatar" src="/img/avatar.webp" alt="avatar">` +
            `<div class="header-user-name">` +
            `<span>${this.username}</span>` +
            `</div>` +
            `</div>`;
        menu.appendChild(
            new DOMParser().parseFromString(profile, "text/html").body.firstChild!
        );

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

        this.clickListener = (event: Event) => {
            if (!menu.contains(event.target as Node)) this.hideMenu();
        };

        document.addEventListener("click", this.clickListener!, { once: true });
    }

    /**
     * Hides the user menu dropdown with animation and cleans up event listeners
     * @returns {void}
     */
    hideMenu(): void {
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
    handleClick(action: String): void {
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
    render(): void {
        this.innerHTML =
            `<div id="header-user" class="header-user">` +
            `<svg xmlns="http://www.w3.org/2000/svg" width="28" height="32" viewBox="0 0 448 512"><path fill="currentColor" d="M224 256a128 128 0 1 0 0-256a128 128 0 1 0 0 256m-96 55.2C54 332.9 0 401.3 0 482.3C0 498.7 13.3 512 29.7 512h388.6c16.4 0 29.7-13.3 29.7-29.7c0-81-54-149.4-128-171.1V362c27.6 7.1 48 32.2 48 62v40c0 8.8-7.2 16-16 16h-16c-8.8 0-16-7.2-16-16s7.2-16 16-16v-24c0-17.7-14.3-32-32-32s-32 14.3-32 32v24c8.8 0 16 7.2 16 16s-7.2 16-16 16h-16c-8.8 0-16-7.2-16-16v-40c0-29.8 20.4-54.9 48-62v-57.1q-9-.9-18.3-.9h-91.4q-9.3 0-18.3.9v65.4c23.1 6.9 40 28.3 40 53.7c0 30.9-25.1 56-56 56s-56-25.1-56-56c0-25.4 16.9-46.8 40-53.7zM144 448a24 24 0 1 0 0-48a24 24 0 1 0 0 48"/></svg>` +
            `</div>`;
    }
}
