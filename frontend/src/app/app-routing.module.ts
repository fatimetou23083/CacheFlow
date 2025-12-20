import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ProductListComponent } from './components/product-list/product-list.component';
import { ProductFormComponent } from './components/product-form/product-form.component';
import { WeatherComponent } from './components/weather/weather.component';
import { AuthComponent } from './components/auth/auth.component';
import { CurrencyListComponent } from './components/currency-list/currency-list.component';
import { AuthGuard } from './auth.guard';

const routes: Routes = [
  { path: '', redirectTo: '/weather', pathMatch: 'full' },
  { path: 'weather', component: WeatherComponent, canActivate: [AuthGuard] },
  { path: 'products', component: ProductListComponent, canActivate: [AuthGuard] },
  { path: 'products/new', component: ProductFormComponent, canActivate: [AuthGuard] },
  { path: 'products/edit/:id', component: ProductFormComponent, canActivate: [AuthGuard] },
  { path: 'auth', component: AuthComponent },
  { path: 'currency', component: CurrencyListComponent, canActivate: [AuthGuard] }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
