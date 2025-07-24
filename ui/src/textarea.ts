export class Textarea extends HTMLElement {
  connectedCallback() {
    this.addListeners();
  }

  addListeners() {
    this.querySelector("textarea")?.addEventListener("input", this.onInput);
  }

  removeListeners() {
    this.querySelector("textarea")?.removeEventListener("input", this.onInput);
  }

  onInput = (event: Event) => {
    const target = event.target as HTMLTextAreaElement;
    this.autoResizeTextarea(target);
  };

  autoResizeTextarea(textarea: HTMLTextAreaElement) {
    textarea.style.height = "auto";
    textarea.style.height = textarea.scrollHeight + "px";
  }
}
