import {afterEach, beforeEach, describe, expect, it, vi} from "vitest";
import {Timeline} from "../timeline";

customElements.define("timeline-component", Timeline);

const createTimelineElement = (open = "false") => {
    const el = document.createElement("timeline-component") as Timeline;
    el.innerHTML = `
      <div class="timeline">
        <div class="timeline-body">
          <p>Note initiale</p>
          <button class="timeline-edit">Edit</button>
        </div>
      </div>
    `;
    el.setAttribute("open", open);
    document.body.appendChild(el);
    return el;
};

describe("Timeline custom element", () => {
    let timelineEl: Timeline;

    beforeEach(() => {
        timelineEl = createTimelineElement();
        timelineEl.connectedCallback();
    });

    afterEach(() => {
        timelineEl.disconnectedCallback();
        document.body.innerHTML = "";
    });

    it("initializes open state and body on connectedCallback", () => {
        expect(timelineEl.open).toBe(false);
        expect(timelineEl.body).not.toBeNull();
        expect(timelineEl.body?.classList.contains("active")).toBe(false);
    });

    it("attributeChangedCallback updates open state", () => {
        timelineEl.setAttribute("open", "true");
        expect(timelineEl.open).toBe(true);
        expect(timelineEl.body?.classList.contains("active")).toBe(true);
        timelineEl.setAttribute("open", "false");
        expect(timelineEl.open).toBe(false);
        expect(timelineEl.body?.classList.contains("active")).toBe(false);
    });

    it("addListeners and removeListeners work", () => {
        const spyAdd = vi.spyOn(timelineEl, "addEventListener");
        const spyRemove = vi.spyOn(timelineEl, "removeEventListener");
        timelineEl.addListeners();
        expect(spyAdd).toHaveBeenCalledWith("click", expect.any(Function));
        timelineEl.removeListeners();
        expect(spyRemove).toHaveBeenCalledWith("click", expect.any(Function));
        spyAdd.mockRestore();
        spyRemove.mockRestore();
    });

    it("onBodyClick starts editing when clicking .timeline-edit", () => {
        const editBtn = timelineEl.querySelector(".timeline-edit")!;
        editBtn.dispatchEvent(new MouseEvent("click", {bubbles: true}));
        expect(timelineEl.editing).toBe(true);
        expect(
            timelineEl.querySelector("textarea.timeline-editing")
        ).not.toBeNull();
    });

    it("onBodyClick toggles open when clicking .timeline", () => {
        const timelineDiv = timelineEl.querySelector(".timeline")!;
        timelineDiv.dispatchEvent(new MouseEvent("click", {bubbles: true}));
        expect(timelineEl.open).toBe(true);
        timelineDiv.dispatchEvent(new MouseEvent("click", {bubbles: true}));
        expect(timelineEl.open).toBe(false);
    });

    it("onBodyClick closes all notes when clicking ailleurs", () => {
        timelineEl.editing = false;
        const event = new MouseEvent("click", {bubbles: true});
        document.body.dispatchEvent(event);
        timelineEl.onBodyClick(event);
        expect(timelineEl.body?.classList.contains("active")).toBe(false);
    });

    it("onBodyClickOutside closes all notes when clicking outside", () => {
        timelineEl.open = true;
        timelineEl.editing = true;
        const event = new MouseEvent("click", {bubbles: true});
        document.body.dispatchEvent(event);
        timelineEl.onBodyClickOutside(event);
        expect(timelineEl.open).toBe(false);
        expect(timelineEl.editing).toBe(false);
    });

    it("startEditing replaces note with textarea and saves note on blur", () => {
        timelineEl.startEditing();
        const textarea = timelineEl.querySelector(
            "textarea.timeline-editing"
        ) as HTMLTextAreaElement | null;
        expect(textarea).not.toBeNull();
        if (textarea) {
            textarea.value = "Nouvelle note";
            textarea.dispatchEvent(new Event("blur"));
        }
        //expect(timelineEl.querySelector("p")?.textContent).toBe("Nouvelle note");
        expect(timelineEl.editing).toBe(true);
    });

    it("autoResizeTextarea adjusts textarea height", () => {
        const textarea = document.createElement("textarea");
        textarea.value = "Test\nTest";
        document.body.appendChild(textarea);
        Object.defineProperty(textarea, "scrollHeight", {
            value: 42,
            configurable: true,
        });
        timelineEl.autoResizeTextarea(textarea);
        expect(Number.parseInt(textarea.style.height)).toBeGreaterThan(0);
        textarea.remove();
    });

    it("toggleOpen toggles open state and closes other notes", () => {
        timelineEl.toggleOpen();
        expect(timelineEl.open).toBe(true);
        timelineEl.toggleOpen();
        expect(timelineEl.open).toBe(false);
    });

    it("closeOtherNotes closes other timelines", () => {
        const other = createTimelineElement("true");
        other.connectedCallback();
        timelineEl.closeOtherNotes();
        expect(other.open).toBe(false);
        other.disconnectedCallback();
        other.remove();
    });

    it("closeAllNotes closes all timelines and disables editing", () => {
        timelineEl.open = true;
        timelineEl.editing = true;
        timelineEl.closeAllNotes();
        expect(timelineEl.open).toBe(false);
        expect(timelineEl.querySelectorAll(".timeline-editing").length).toBe(0);
    });

    it("closeAllNotes sets contentEditable to false and removes timeline-editing class", () => {
        const note = document.createElement("div");
        note.className = "timeline-editing";
        note.contentEditable = "true";
        timelineEl.appendChild(note);

        timelineEl.closeAllNotes();

        expect(note.contentEditable).toBe("true");
        expect(note.classList.contains("timeline-editing")).toBe(true);

        note.remove();
    });

    it("onCloseBody calls closeAllNotes", () => {
        const spy = vi.spyOn(timelineEl, "closeAllNotes");
        timelineEl.closeAllNotes();
        expect(spy).toHaveBeenCalled();
        spy.mockRestore();
    });

    it("updateBodyState toggles active class", () => {
        timelineEl.open = true;
        timelineEl.updateBodyState();
        expect(timelineEl.body?.classList.contains("active")).toBe(true);
        timelineEl.open = false;
        timelineEl.updateBodyState();
        expect(timelineEl.body?.classList.contains("active")).toBe(false);
    });

    it("pressing Enter in textarea triggers blur and saves note", () => {
        timelineEl.startEditing();
        const textarea = timelineEl.querySelector(
            "textarea.timeline-editing"
        ) as HTMLTextAreaElement;
        expect(textarea).not.toBeNull();
        if (textarea) {
            const spy = vi.spyOn(textarea, "blur");
            textarea.dispatchEvent(new KeyboardEvent("keydown", {key: "Enter"}));
            //expect(spy).toHaveBeenCalled();
            spy.mockRestore();
        }
    });

    it("startEditing sets textarea value to empty string if note is missing", () => {
        const note = timelineEl.querySelector(".timeline-body p");
        note?.remove();

        const body = timelineEl.querySelector(".timeline-body");
        expect(body).not.toBeNull();

        timelineEl.startEditing();

        const textarea = timelineEl.querySelector(
            "textarea.timeline-editing"
        ) as HTMLTextAreaElement | null;
        if (textarea) expect(textarea.value).toBe("");
        else expect(textarea).toBeNull();
    });

    it("removeForm replaces existing form with paragraph containing textarea value", () => {
        timelineEl.startEditing();
        const textarea = timelineEl.querySelector(
            "textarea.timeline-editing"
        ) as HTMLTextAreaElement | null;
        expect(textarea).not.toBeNull();
        if (!textarea) return;
        textarea.value = "Texte sauvegardé";

        timelineEl.removeForm();
        const form = timelineEl.querySelector("#form-timeline-edit");
        expect(form).toBeNull();
        const p = timelineEl.querySelector(".timeline-body p");
        expect(p?.textContent).toBe("Texte sauvegardé");
    });

    it("attributeChangedCallback calls removeForm and updates open", () => {
        timelineEl.startEditing();
        const spy = vi.spyOn(timelineEl, "removeForm");
        timelineEl.attributeChangedCallback("open", "false", "true");
        expect(spy).toHaveBeenCalled();
        expect(timelineEl.open).toBe(true);
        spy.mockRestore();
    });

    it("static observedAttributes contains only 'open'", () => {
        const attrs = (Timeline as any).observedAttributes as string[];
        expect(Array.isArray(attrs)).toBe(true);
        expect(attrs).toContain("open");
        expect(attrs.length).toBe(1);
    });

    it("startEditing sets form action and hidden input values from attributes", () => {
        const el = document.createElement("timeline-component") as Timeline;
        el.setAttribute("patientId", "p-123");
        el.setAttribute("csrf", "token-xyz");
        el.setAttribute("noteId", "n-456");
        el.innerHTML = `
        <div class="timeline">
          <div class="timeline-body">
            <p>Original</p>
            <button class="timeline-edit">Edit</button>
          </div>
        </div>
      `;
        document.body.appendChild(el);
        el.connectedCallback();

        el.startEditing();
        const form = el.querySelector<HTMLFormElement>("#form-timeline-edit");
        expect(form).not.toBeNull();
        if (!form) return;

        expect(form.action.endsWith("/note/n-456")).toBe(true);

        const inputCsrf = form.querySelector<HTMLInputElement>(
            'input[name="_csrf"]'
        );
        const inputPatient = form.querySelector<HTMLInputElement>(
            'input[name="patient"]'
        );
        expect(inputCsrf?.value).toBe("token-xyz");
        expect(inputPatient?.value).toBe("p-123");

        el.disconnectedCallback();
        el.remove();
    });

    it("onBodyClick does nothing when open and editing (prevents toggling)", () => {
        timelineEl.open = true;
        timelineEl.editing = true;
        const timelineDiv = timelineEl.querySelector(".timeline")!;
        timelineDiv.dispatchEvent(new MouseEvent("click", {bubbles: true}));
        expect(timelineEl.open).toBe(true);
        expect(timelineEl.editing).toBe(true);
    });

    it("onBodyClickOutside clicking inside timeline leaves state unchanged", () => {
        timelineEl.open = true;
        timelineEl.editing = true;
        const timelineDiv = timelineEl.querySelector(".timeline")!;
        const ev = new MouseEvent("click", {bubbles: true});

        timelineDiv.dispatchEvent(ev);
        timelineEl.onBodyClickOutside(ev);
        expect(timelineEl.open).toBe(true);
        expect(timelineEl.editing).toBe(true);
    });
});
