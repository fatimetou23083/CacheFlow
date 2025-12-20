import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private apiUrl = '/api/auth';
    private currentUserSubject = new BehaviorSubject<any>(null);
    public currentUser$ = this.currentUserSubject.asObservable();

    constructor(private http: HttpClient) {
        this.checkSession();
    }

    checkSession() {
        this.http.get(`${this.apiUrl}/me`, { withCredentials: true }).subscribe({
            next: (user) => this.currentUserSubject.next(user),
            error: () => this.currentUserSubject.next(null)
        });
    }

    login(credentials: any): Observable<any> {
        return this.http.post(`${this.apiUrl}/login`, credentials, { withCredentials: true }).pipe(
            tap(() => this.checkSession())
        );
    }

    register(user: any): Observable<any> {
        return this.http.post(`${this.apiUrl}/register`, user, { withCredentials: true });
    }

    logout(): Observable<any> {
        return this.http.post(`${this.apiUrl}/logout`, {}, { withCredentials: true }).pipe(
            tap(() => this.currentUserSubject.next(null))
        );
    }
}
