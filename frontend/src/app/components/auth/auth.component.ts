import { Component } from '@angular/core';
import { AuthService } from '../../service/auth.service';
import { Router } from '@angular/router';

@Component({
    selector: 'app-auth',
    templateUrl: './auth.component.html',
    styleUrls: ['./auth.component.css']
})
export class AuthComponent {
    isLogin = true;
    credentials = { username: '', password: '' };
    registerData = { username: '', password: '', email: '', role: 'USER' };
    errorMessage = '';

    constructor(private authService: AuthService, private router: Router) { }

    ngOnInit() {
        // Rediriger si déjà connecté
        this.authService.currentUser$.subscribe((user: any) => {
            if (user) {
                this.router.navigate(['/']);
            }
        });
    }

    toggleMode() {
        this.isLogin = !this.isLogin;
        this.errorMessage = '';
    }

    onSubmit() {
        if (this.isLogin) {
            this.authService.login(this.credentials).subscribe({
                next: () => {
                    this.router.navigate(['/']);
                },
                error: (err) => {
                    console.error('Login error:', err);
                    let msg = 'Identifiants invalides';
                    if (err.status === 0) {
                        msg = 'Impossible de contacter le serveur. Vérifiez votre connexion.';
                    } else if (err.error) {
                        msg = typeof err.error === 'string' ? err.error : (err.error.message || JSON.stringify(err.error));
                    }
                    this.errorMessage = 'Connexion échouée : ' + msg;
                }
            });
        } else {
            this.authService.register(this.registerData).subscribe({
                next: () => {
                    this.toggleMode();
                    this.errorMessage = 'Inscription réussie ! Vous pouvez maintenant vous connecter.';
                },
                error: (err) => {
                    console.error('Registration error:', err);
                    let msg = 'Erreur inconnue';
                    if (err.status === 0) {
                        msg = 'Impossible de contacter le serveur. Vérifiez votre connexion.';
                    } else if (err.error) {
                        msg = typeof err.error === 'string' ? err.error : (err.error.message || JSON.stringify(err.error));
                    }
                    this.errorMessage = 'Échec de l\'enregistrement : ' + msg;
                }
            });
        }
    }
}
