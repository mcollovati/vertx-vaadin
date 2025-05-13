import { Route } from '@vaadin/router';
import './views/hilla/main-layout';
import './views/hilla/crud-view';


export type ViewRoute = Route & {
  title?: string;
  icon?: string;
  children?: ViewRoute[];
};

export const views: ViewRoute[] = [
  // place routes below (more info https://hilla.dev/docs/routing)
  {
    path: '',
    component: 'hilla-dashboard-view',
    icon: 'la la-chart-area',
    title: 'Dashboard',
    action: async () => {
      await import('./views/hilla/dashboard-view');
    },
  },
  {
    path: 'crud',
    component: 'hilla-crud-view',
    icon: 'la la-columns',
    title: 'CRUD',
  },
];
export const routes: ViewRoute[] = [
  {
    path: '',
    component: 'hilla-main-layout',
    children: [...views],
  },
];
