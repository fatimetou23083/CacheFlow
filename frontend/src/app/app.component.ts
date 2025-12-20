import { Component } from '@angular/core';
import { AuthService } from './service/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  constructor(public authService: AuthService, private router: Router) { }

  logout() {
    this.authService.logout().subscribe({
      next: () => this.router.navigate(['/auth']),
      error: () => this.router.navigate(['/auth']), // Force logout on error
      complete: () => this.router.navigate(['/auth']) // Ensure navigation happens
    });
  }
}
