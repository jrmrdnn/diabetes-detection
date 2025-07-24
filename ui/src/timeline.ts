export class Timeline extends HTMLElement {
    editing = false;
    open = false;
    csrf: string | null = null;
    patientId: string | null = null;
    noteId: string | null = null;
    body: HTMLElement | null = null;
    form: HTMLFormElement | null = null;

    static get observedAttributes() {
        return ["open"] as const;
    }

    attributeChangedCallback(name: string, _oldValue: string, newValue: string) {
        this.removeForm();
        if (name === "open") {
            this.open = newValue === "true";
            this.updateBodyState();
        }
    }

    connectedCallback() {
        this.open = this.getAttribute("open") === "true";
        this.csrf = this.getAttribute("csrf");
        this.patientId = this.getAttribute("patientId");
        this.noteId = this.getAttribute("noteId");
        this.body = this.querySelector<HTMLElement>(".timeline-body");
        this.updateBodyState();
        this.addListeners();
    }

    disconnectedCallback() {
        this.removeListeners();
    }

    addListeners() {
        this.addEventListener("click", this.onBodyClick);
        document.addEventListener("click", this.onBodyClickOutside);
        window.addEventListener("resize", this.closeAllNotes);
    }

    removeListeners() {
        this.removeEventListener("click", this.onBodyClick);
        document.removeEventListener("click", this.onBodyClickOutside);
        window.removeEventListener("resize", this.closeAllNotes);
    }

    onBodyClick = (event: MouseEvent) => {
        if (this.open && this.editing) return;

        const target = event.target as HTMLElement;

        if (target.closest(".timeline-edit")) {
            event.stopPropagation();
            this.startEditing();
            return;
        }

        if (target.closest(".timeline")) {
            event.stopPropagation();
            this.toggleOpen();
            return;
        }

        this.closeAllNotes();
    };

    onBodyClickOutside = (event: MouseEvent) => {
        const target = event.target as HTMLElement;
        if (target.closest(".timeline")) return;
        if (this.editing) this.removeForm();
        this.editing = false;
        this.closeAllNotes();
    };

    startEditing() {
        this.closeAllNotes();

        const note = this.querySelector<HTMLElement>(".timeline-body p");

        this.open = true;
        this.editing = true;
        this.setAttribute("open", "true");

        // Création du formulaire
        this.form = document.createElement("form");
        this.form.id = "form-timeline-edit";
        this.form.method = "post";
        this.form.action = `/note/${this.noteId}`;

        // Champ pour méthode PUT
        const inputMethod = document.createElement("input");
        inputMethod.type = "hidden";
        inputMethod.name = "_method";
        inputMethod.value = "PUT";

        // Champ patientId
        const inputPatient = document.createElement("input");
        inputPatient.type = "hidden";
        inputPatient.name = "patient";
        inputPatient.value = this.patientId ?? "";

        // Champ CSRF
        const inputCsrf = document.createElement("input");
        inputCsrf.type = "hidden";
        inputCsrf.name = "_csrf";
        inputCsrf.value = this.csrf ?? "";

        // Zone de texte pour édition
        const textarea = document.createElement("textarea");
        textarea.name = "note";
        textarea.className = "timeline-editing";
        textarea.value = note?.textContent || "";
        textarea.minLength = 10;
        textarea.maxLength = 1_000;
        textarea.required = true;

        // Bouton de soumission
        const button = document.createElement("button");
        button.type = "submit";
        button.className = "btn positive";
        button.textContent = "Sauvegarder";

        // Ajout des éléments au formulaire
        this.form.append(inputMethod, inputPatient, inputCsrf, textarea, button);

        // Remplacement du contenu
        note?.replaceWith(this.form);

        textarea.focus();
        this.autoResizeTextarea(textarea);

        textarea.addEventListener("input", () => this.autoResizeTextarea(textarea));
    }

    autoResizeTextarea(textarea: HTMLTextAreaElement) {
        textarea.style.height = "auto";
        textarea.style.height = textarea.scrollHeight + "px";
    }

    toggleOpen() {
        this.open = !this.open;
        this.setAttribute("open", String(this.open));
        this.closeOtherNotes();
    }

    closeOtherNotes() {
        document
            .querySelectorAll<Timeline>("timeline-component")
            .forEach((timeline) =>
                timeline !== this &&
                timeline.open &&
                timeline.setAttribute("open", "false")
            );
    }

    closeAllNotes() {
        document
            .querySelectorAll<Timeline>("timeline-component")
            .forEach((timeline) => timeline.setAttribute("open", "false"));
    }

    updateBodyState() {
        if (this.body) this.body.classList.toggle("active", this.open);
    }

    removeForm() {
        const form = this.querySelector<HTMLFormElement>("#form-timeline-edit");
        if (form) {
            const textarea = form.querySelector<HTMLTextAreaElement>("textarea");
            const text = textarea?.value || "";
            const p = document.createElement("p");
            p.textContent = text;
            form.replaceWith(p);
        }
    }
}
