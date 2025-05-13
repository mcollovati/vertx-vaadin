import '@vaadin/button';
import '@vaadin/grid';
import '@vaadin/split-layout';
import '@vaadin/grid/vaadin-grid-sort-column.js';
import '@vaadin/text-field';
import '@vaadin/email-field';
import '@vaadin/date-picker';
import { GridActiveItemChangedEvent } from '@vaadin/grid';
import { html } from 'lit';
import { View } from './view';
import { customElement, state } from 'lit/decorators.js';
import { Binder, field } from '@vaadin/hilla-lit-form';

import Person from 'Frontend/generated/org/vaadin/crudui/demo/entity/User';
import PersonModel from 'Frontend/generated/org/vaadin/crudui/demo/entity/UserModel';
import { CrudEndpoint } from 'Frontend/generated/endpoints';



@customElement('hilla-crud-view')
export class CrudView extends View {
  @state() people: Person[] = [];
  @state() selected?: Person;
  binder = new Binder(this, PersonModel);

  async firstUpdated() {
    //-this.classList.add('h-full', 'w-full');
    let data = await CrudEndpoint.findAll();
    if(data.length>0){
      this.people = data;
    }

  }

  render() {
    const { model } = this.binder;

    return html`
      <vaadin-split-layout class="h-full w-full">
        <vaadin-grid
          class="people-grid h-full"
          .items=${this.people}
          .selectedItems=${[this.selected]}
          @active-item-changed=${this.activeItemChanged}>
          <vaadin-grid-sort-column path="name" auto-width></vaadin-grid-sort-column>
          <vaadin-grid-sort-column path="phoneNumber" auto-width></vaadin-grid-sort-column>
          <vaadin-grid-sort-column path="email" auto-width></vaadin-grid-sort-column>
          <vaadin-grid-sort-column path="birthDate" auto-width></vaadin-grid-sort-column>
          <vaadin-grid-sort-column path="mainGroup" auto-width></vaadin-grid-sort-column>
        </vaadin-grid>

        <div class="flex flex-col gap-s p-m" style="width: 35%;" ?hidden=${!this.selected}>
          <vaadin-text-field label="First name" ${field(model.name)}></vaadin-text-field>
          <vaadin-text-field label="Phone number" ${field(model.phoneNumber)}></vaadin-text-field>
          <vaadin-email-field label="Email" ${field(model.email)}></vaadin-email-field>
          <vaadin-date-picker label="Date of birth" ${field(model.birthDate)}></vaadin-date-picker>
          <vaadin-text-field label="Occupation" ${field(model.mainGroup)}></vaadin-text-field>

          <div class="flex gap-m">
            <vaadin-button theme="primary" @click=${this.save}>Save</vaadin-button>
            <vaadin-button theme="tertiary" @click=${this.cancelEdit}>Cancel</vaadin-button>
          </div>
        </div>
      </vaadin-split-layout>
    `;
  }

  async save() {
    const saved = await this.binder.submitTo(CrudEndpoint.save);
    if (saved) {
      this.people = this.people.map((p) => (p.id === saved.id ? saved : p));
    }
  }

  cancelEdit() {
    this.selected = undefined;
    this.binder.clear();
  }

  activeItemChanged(e: GridActiveItemChangedEvent<Person>) {
    this.selected = e.detail.value as Person;
    if (this.selected) {
      this.binder.read(this.selected);
    }
  }
}
