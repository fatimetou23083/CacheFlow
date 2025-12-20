import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import * as SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

@Injectable({
    providedIn: 'root'
})
export class NotificationService {
    private apiUrl = '/api/notifications';
    private stompClient: any | null = null;
    private notificationSubject = new Subject<any>();
    public notifications$ = this.notificationSubject.asObservable();

    constructor(private http: HttpClient) {
        this.connect();
    }

    connect() {
        const socketFactory = () => new SockJS('/ws');
        this.stompClient = Stomp.over(socketFactory);

        // Disable debug logs
        this.stompClient.debug = () => { };

        this.stompClient.connectHeaders = {};
        this.stompClient.onConnect = (frame: any) => {
            // console.log('Connected: ' + frame);
            this.stompClient?.subscribe('/topic/notifications', (message: any) => {
                this.notificationSubject.next(JSON.parse(message.body));
            });
        };

        this.stompClient.onStompError = (frame: any) => {
            console.error('STOMP error:', frame);
        };

        this.stompClient.activate();
    }

    sendNotification(notification: any): Observable<any> {
        return this.http.post(`${this.apiUrl}/send`, notification, { withCredentials: true });
    }

    getAll(): Observable<any[]> {
        return this.http.get<any[]>(this.apiUrl, { withCredentials: true });
    }
}
