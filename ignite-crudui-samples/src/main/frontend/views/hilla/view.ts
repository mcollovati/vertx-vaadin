import { MobxLitElement } from '@adobe/lit-mobx';
import { applyTheme } from 'Frontend/generated/theme';
import { autorun, IAutorunOptions, IReactionDisposer, IReactionOptions, IReactionPublic, reaction } from 'mobx';

export class MobxElement extends MobxLitElement {
  private disposers: IReactionDisposer[] = [];

  /**
   * Creates a MobX reaction using the given parameters and disposes it when this element is detached.
   *
   * This should be called from `connectedCallback` to ensure that the reaction is active also if the element is attached again later.
   */
  protected reaction<T, FireImmediately extends boolean = false>(
    expression: (r: IReactionPublic) => T,
    effect: (arg: T, prev: FireImmediately extends true ? T | undefined : T, r: IReactionPublic) => void,
    opts?: IReactionOptions<T, FireImmediately>
  ): void {
    this.disposers.push(reaction(expression, effect, opts));
  }

  /**
   * Creates a MobX autorun using the given parameters and disposes it when this element is detached.
   *
   * This should be called from `connectedCallback` to ensure that the reaction is active also if the element is attached again later.
   */
  protected autorun(view: (r: IReactionPublic) => any, opts?: IAutorunOptions): void {
    this.disposers.push(autorun(view, opts));
  }

  disconnectedCallback(): void {
    super.disconnectedCallback();
    this.disposers.forEach((disposer) => {
      disposer();
    });
    this.disposers = [];
  }
}

/**
 * A view is a container that holds all UI elements, layouts and styling of a section of the application. A view is
 * usually mapped under a certain URL.
 *
 * By default, views don't use shadow root to render their children, which means that any elements added directly to a
 * view are rendered into the light DOM. This is important not just for enabling the global CSS to cascade naturally to
 * the view, but also to allow external tools to scan the document, such as screen readers, search engine bots, activity
 * trackers and automated testing scripts, for example.
 *
 * The view class also brings the MobX dependency for state management.
 */
export class View extends MobxElement {
  createRenderRoot(): HTMLElement | DocumentFragment {
    // Do not use a shadow root
    return this;
  }
}

/**
 * A layout is a container that organizes UI elements in a certain way, and uses shadow root to render its children.
 * <slot> elements can be used to determine where the child elements are rendered.
 *
 * The application theme is applied to the shadow root by adopting the theme style sheets defined in the global scope.
 * Styles defined outside of the theme are not applied.
 *
 * The layout class also bring the MobX dependency for state management.
 */
export class Layout extends MobxElement {
  connectedCallback(): void {
    super.connectedCallback();
    applyTheme(this.shadowRoot as ShadowRoot);
  }
}
