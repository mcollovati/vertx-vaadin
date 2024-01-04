import 'Frontend/generated/jar-resources/copilot/copilot.js';
// @ts-ignore
if (import.meta.hot) {
  // @ts-ignore
  import.meta.hot.on('vite:afterUpdate', () => {
    (window as any).Vaadin.copilotPlugins._internals.copilotEventBus.emit('vite-after-update',{});
  });
}

import '@vaadin/vertical-layout/src/vaadin-vertical-layout.js';
import '@vaadin/context-menu/src/vaadin-context-menu.js';
import '@vaadin/icons/vaadin-iconset.js';
import '@vaadin/menu-bar/src/vaadin-menu-bar.js';
import '@vaadin/icon/vaadin-icon.js';
import '@vaadin/text-field/src/vaadin-text-field.js';
import '@vaadin/grid/src/vaadin-grid.js';
import '@vaadin/grid/src/vaadin-grid-tree-column.js';
import './vaadin-featureflags.js';

import './index';

import 'Frontend/generated/jar-resources/vaadin-dev-tools/vaadin-dev-tools.js';
