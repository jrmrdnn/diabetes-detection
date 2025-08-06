import {afterEach, beforeEach, describe, expect, it, vi} from "vitest";
import {Textarea} from "../textarea";

const TAG = "x-textarea";

if (!customElements.get(TAG)) customElements.define(TAG, Textarea);

describe("Textarea custom element", () => {
    beforeEach(() => {
        document.body.innerHTML = "";
    });

    afterEach(() => {
        document.body.innerHTML = "";
        vi.restoreAllMocks();
    });

    function createHost(scrollHeight = 100) {
        const host = document.createElement(TAG) as unknown as Textarea;
        const ta = document.createElement("textarea");
        Object.defineProperty(ta, "scrollHeight", {
            value: scrollHeight,
            configurable: true,
        });
        host.appendChild(ta);
        return {host, ta};
    }

    it("attaches input listener on connectedCallback and calls onInput when input event fires", () => {
        const {host, ta} = createHost(50);
        const onInputSpy = vi.spyOn(host, "onInput");

        document.body.appendChild(host);
        ta.dispatchEvent(new Event("input", {bubbles: true}));
        expect(onInputSpy).toHaveBeenCalled();
    });

    it("removeListeners removes the input listener so input events no longer call onInput", () => {
        const {host, ta} = createHost(60);
        const onInputSpy = vi.spyOn(host, "onInput");
        document.body.appendChild(host);

        ta.dispatchEvent(new Event("input", {bubbles: true}));
        expect(onInputSpy).toHaveBeenCalledTimes(1);

        host.removeListeners();
        ta.dispatchEvent(new Event("input", {bubbles: true}));
        expect(onInputSpy).toHaveBeenCalledTimes(1);
    });

    it("onInput calls autoResizeTextarea with the textarea target", () => {
        const {host, ta} = createHost(77);
        const resizeSpy = vi.spyOn(host, "autoResizeTextarea");
        document.body.appendChild(host);
        ta.dispatchEvent(new Event("input", {bubbles: true}));
        expect(resizeSpy).toHaveBeenCalledWith(ta);
    });

    it("autoResizeTextarea sets the textarea height to its scrollHeight in px", () => {
        const {host, ta} = createHost(123);
        host.autoResizeTextarea(ta);
        expect(ta.style.height).toBe("123px");
    });
});
