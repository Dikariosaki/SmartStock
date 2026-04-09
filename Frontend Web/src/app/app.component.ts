import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AppToastComponent } from '@shared/ui/toast/app-toast.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, AppToastComponent],
  template: `
    <router-outlet></router-outlet>
    <app-toast></app-toast>
  `,
  styles: [
    `
      :host {
        display: block;
        min-height: 100vh;
      }
    `,
  ],
})
export class AppComponent {
  title = 'SmartStock';
}
