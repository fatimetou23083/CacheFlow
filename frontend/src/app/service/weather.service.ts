import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Weather } from '../model/weather';

@Injectable({
  providedIn: 'root'
})
export class WeatherService {
  private apiUrl = '/api/weather';
  private httpOptions = {
    headers: new HttpHeaders({
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    })
  };

  constructor(private http: HttpClient) { }

  /**
   * Récupère la météo pour une ville donnée (utilise le cache)
   * GET /api/weather/{city}
   */
  getWeather(city: string): Observable<Weather> {
    return this.http.get<Weather>(`${this.apiUrl}/${encodeURIComponent(city)}`, this.httpOptions);
  }

  /**
   * Force la mise à jour du cache météo pour une ville donnée
   * POST /api/weather/refresh/{city}
   */
  refreshWeather(city: string): Observable<Weather> {
    return this.http.post<Weather>(`${this.apiUrl}/refresh/${encodeURIComponent(city)}`, {}, this.httpOptions);
  }
}

