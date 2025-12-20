import { Component } from '@angular/core';
import { Weather } from '../../model/weather';
import { WeatherService } from '../../service/weather.service';

@Component({
  selector: 'app-weather',
  templateUrl: './weather.component.html',
  styleUrls: ['./weather.component.css']
})
export class WeatherComponent {
  city: string = '';
  weather: Weather | null = null;
  loading = false;
  error: string | null = null;
  refreshing = false;

  constructor(private weatherService: WeatherService) { }

  /**
   * Récupère la météo pour la ville saisie (utilise le cache)
   */
  getWeather(): void {
    if (!this.city || this.city.trim() === '') {
      this.error = 'Veuillez saisir le nom d\'une ville';
      return;
    }

    this.loading = true;
    this.error = null;
    this.weather = null;

    this.weatherService.getWeather(this.city.trim()).subscribe({
      next: (weather) => {
        this.weather = weather;
        this.loading = false;
        console.log('Météo récupérée:', weather);
      },
      error: (err) => {
        console.error('Erreur lors de la récupération de la météo:', err);
        this.error = this.extractErrorMessage(err);
        this.loading = false;
        this.weather = null;
      }
    });
  }

  /**
   * Force la mise à jour du cache météo pour la ville actuelle
   */
  refreshCache(): void {
    if (!this.city || this.city.trim() === '') {
      this.error = 'Veuillez d\'abord rechercher une ville';
      return;
    }

    if (!this.weather) {
      this.error = 'Aucune météo à rafraîchir';
      return;
    }

    this.refreshing = true;
    this.error = null;

    this.weatherService.refreshWeather(this.city.trim()).subscribe({
      next: (weather) => {
        this.weather = weather;
        this.refreshing = false;
        console.log('Cache rafraîchi:', weather);
      },
      error: (err) => {
        console.error('Erreur lors du rafraîchissement du cache:', err);
        this.error = this.extractErrorMessage(err);
        this.refreshing = false;
      }
    });
  }

  /**
   * Extrait le message d'erreur depuis la réponse HTTP
   */
  private extractErrorMessage(err: any): string {
    if (err.error && err.error.message) {
      return err.error.message;
    }
    if (err.error && err.error.error) {
      return err.error.error + (err.error.message ? ': ' + err.error.message : '');
    }
    if (err.message) {
      return err.message;
    }
    if (err.status === 404) {
      return 'Ville non trouvée. Vérifiez le nom de la ville.';
    }
    if (err.status === 0) {
      return 'Impossible de se connecter au serveur. Vérifiez que le backend est démarré.';
    }
    return 'Une erreur est survenue lors de la récupération de la météo.';
  }

  /**
   * Formate la date pour l'affichage
   */
  formatDate(dateString: string): string {
    if (!dateString) return '';
    try {
      const date = new Date(dateString);
      return date.toLocaleString('fr-FR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch (e) {
      return dateString;
    }
  }

  /**
   * Gère la soumission du formulaire avec la touche Entrée
   */
  onKeyPress(event: KeyboardEvent): void {
    if (event.key === 'Enter') {
      this.getWeather();
    }
  }
}

