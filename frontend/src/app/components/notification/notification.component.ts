import { Component, OnInit } from '@angular/core';
import { NotificationService } from '../../service/notification.service';

@Component({
    selector: 'app-notification',
    templateUrl: './notification.component.html',
    styleUrls: ['./notification.component.css']
})
export class NotificationComponent implements OnInit {
    notifications: any[] = [];
    show = false;

    constructor(private notificationService: NotificationService) { }

    ngOnInit(): void {
        // Load initial history
        this.notificationService.getAll().subscribe(data => {
            this.notifications = data;
        });

        // Subscribe to new notifications
        this.notificationService.notifications$.subscribe(notification => {
            this.notifications.unshift(notification);
            this.showToast(notification);
        });
    }

    showToast(notification: any) {
        this.show = true;
        setTimeout(() => this.show = false, 3000);
    }
}
