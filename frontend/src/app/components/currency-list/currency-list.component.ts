import { Component, OnInit } from '@angular/core';
import { Currency, CurrencyService } from '../../service/currency.service';

@Component({
    selector: 'app-currency-list',
    templateUrl: './currency-list.component.html',
    styleUrls: ['./currency-list.component.css']
})
export class CurrencyListComponent implements OnInit {
    currencies: Currency[] = [];
    convertedAmount: number | null = null;
    fromCurrency = 'USD';
    toCurrency = 'EUR';
    amount = 1;

    constructor(private currencyService: CurrencyService) { }

    ngOnInit(): void {
        this.currencyService.getAllCurrencies().subscribe({
            next: (data) => {
                this.currencies = data;
                console.info('Currencies loaded:', data.length);
            },
            error: (err) => {
                console.error('Error fetching currencies:', err);
                // Fallback or empty state already handled by currencies = []
            }
        });
    }

    convert() {
        if (!this.amount || this.amount <= 0) return;

        this.currencyService.convert(this.fromCurrency, this.toCurrency, this.amount).subscribe({
            next: (result: any) => {
                // The backend returns an object { convertedAmount: 123.45, ... }
                if (result && result.convertedAmount !== undefined) {
                    this.convertedAmount = result.convertedAmount;
                } else {
                    console.error('Invalid response format:', result);
                }
            },
            error: (err) => {
                console.error('Conversion failed:', err);
            }
        });
    }
}
