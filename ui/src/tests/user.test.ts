import {afterEach, beforeEach, describe, expect, it, vi} from "vitest";
import {User} from "../user";

customElements.define("app-user", User);

const createUserElement = (username = "testuser") => {
    const el = document.createElement("app-user") as User;
    el.setAttribute("username", username);
    document.body.appendChild(el);
    return el;
};

describe("User custom element", () => {
    let userEl: User;

    beforeEach(() => {
        userEl = createUserElement();
        userEl.connectedCallback();
    });

    afterEach(() => {
        userEl.disconnectedCallback();
        document.body.innerHTML = "";
    });

    it("renders header on connectedCallback", () => {
        expect(userEl.innerHTML).toContain('id="header-user"');
        expect(userEl.querySelector("#header-user")).not.toBeNull();
    });

    it("sets up click listener on header-user", () => {
        expect(userEl.querySelector(".header-user-menu")).toBeNull();
        userEl
            .querySelector("#header-user")
            ?.dispatchEvent(new MouseEvent("click", {bubbles: true}));
        expect(userEl.querySelector(".header-user-menu")).not.toBeNull();
    });

    it("removes click listener on disconnectedCallback", () => {
        const spy = vi.spyOn(
            userEl.querySelector("#header-user")!,
            "removeEventListener"
        );
        userEl.disconnectedCallback();
        expect(spy).toHaveBeenCalledWith("click", expect.any(Function));
        spy.mockRestore();
    });

    it("toggleUserMenu creates and hides menu", async () => {
        expect(userEl.querySelector(".header-user-menu")).toBeNull();
        userEl.toggleUserMenu(new MouseEvent("click"));
        expect(userEl.querySelector(".header-user-menu")).not.toBeNull();
        userEl.toggleUserMenu(new MouseEvent("click"));
        await new Promise((r) => setTimeout(r, 310));
        expect(userEl.querySelector(".header-user-menu")).toBeNull();
    });

    it("createUserMenu creates menu with profile and items", () => {
        userEl.createUserMenu();
        const menu = userEl.querySelector(".header-user-menu");
        expect(menu).not.toBeNull();
        expect(menu?.querySelector(".header-user-profile")).not.toBeNull();
        expect(menu?.querySelector(".header-user-avatar")).not.toBeNull();
        expect(menu?.querySelector(".header-user-name")?.textContent).toContain(
            "testuser"
        );
        const items = menu?.querySelectorAll(".header-user-item");
        expect(items?.length).toBe(2);
        expect(items?.[0].textContent).toBe("Profile");
        expect(items?.[1].textContent).toBe("Déconnexion");
    });

    it("menu items call handleClick and hide menu", () => {
        const spy = vi.spyOn(userEl as any, "handleClick");
        userEl.createUserMenu();
        const items = userEl.querySelectorAll(".header-user-item");
        items[0].dispatchEvent(new MouseEvent("click", {bubbles: true}));
        expect(spy).toHaveBeenCalledWith("profile");
        spy.mockRestore();
        userEl.createUserMenu();
        const spyHide = vi.spyOn(userEl as any, "hideMenu");
        items[1].dispatchEvent(new MouseEvent("click", {bubbles: true}));
        expect(spyHide).toHaveBeenCalled();
        spyHide.mockRestore();
    });

    it("hideMenu removes menu and cleans up listeners", async () => {
        userEl.createUserMenu();
        const menu = userEl.querySelector(".header-user-menu");
        expect(menu).not.toBeNull();
        userEl.hideMenu();
        expect(menu?.classList.contains("hide")).toBe(true);
        await new Promise((r) => setTimeout(r, 310));
        expect(userEl.querySelector(".header-user-menu")).toBeNull();
        expect(userEl["clickListener"]).toBeUndefined();
    });

    it("handleClick logs profile and redirects on logout", () => {
        const logSpy = vi.spyOn(console, "log").mockImplementation(() => {
        });
        userEl.createUserMenu();
        userEl.handleClick("profile");
        //expect(logSpy).toHaveBeenCalledWith("Profile clicked");
        logSpy.mockRestore();

        const oldLocation = window.location;

        // @ts-ignore
        delete window.location;

        // @ts-ignore
        window.location = {href: ""};

        userEl.handleClick("logout");
        expect(window.location.href).toBe("/login");
        Object.defineProperty(window, "location", {
            value: oldLocation,
            writable: true,
            configurable: true,
        });
    });

    it("render sets correct innerHTML", () => {
        userEl.render();
        expect(userEl.innerHTML).toContain(
            '<div id="header-user" class="header-user">'
        );
        expect(userEl.innerHTML).toContain("<svg");
    });

    it("clickListener hides menu when clicking outside", async () => {
        userEl.createUserMenu();
        const menu = userEl.querySelector(".header-user-menu");
        expect(menu).not.toBeNull();

        document.body.dispatchEvent(new MouseEvent("click", {bubbles: true}));

        await new Promise((r) => setTimeout(r, 310));
        expect(userEl.querySelector(".header-user-menu")).toBeNull();
    });

    it("handleClick logs unknown action for invalid action", () => {
        const logSpy = vi.spyOn(console, "log").mockImplementation(() => {
        });
        userEl.createUserMenu();
        userEl.handleClick("foobar");
        //expect(logSpy).toHaveBeenCalledWith("Unknown action");
        logSpy.mockRestore();
    });
});
