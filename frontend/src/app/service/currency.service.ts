import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Currency {
  code: string;
  rate: number;
  lastUpdate: string;
}

@Injectable({
  providedIn: 'root'
})
export class CurrencyService {
  private apiUrl = '/api/currencies';

  constructor(private http: HttpClient) { }

  getAllCurrencies(): Observable<Currency[]> {
    return this.http.get<Currency[]>(`${this.apiUrl}/all`);
  }

  convert(from: string, to: string, amount: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/${from}/${to}/${amount}`);
  }
}
