import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Product } from '../../model/product';
import { ProductService } from '../../service/product.service';

@Component({
  selector: 'app-product-form',
  templateUrl: './product-form.component.html',
  styleUrls: ['./product-form.component.css']
})
export class ProductFormComponent implements OnInit {
  product: Product = {
    name: '',
    price: 0,
    category: ''
  };
  isEditMode = false;
  loading = false;
  error: string | null = null;

  constructor(
    private productService: ProductService,
    private route: ActivatedRoute,
    private router: Router
  ) { }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.loadProduct(id);
    }
  }

  loadProduct(id: string): void {
    this.loading = true;
    this.productService.getProductById(id).subscribe({
      next: (product) => {
        this.product = product;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement du produit';
        this.loading = false;
        console.error(err);
      }
    });
  }

  onSubmit(): void {
    if (!this.isFormValid()) {
      this.error = 'Veuillez remplir tous les champs';
      return;
    }

    this.loading = true;
    this.error = null;

    const productToSend = {
      name: this.product.name.trim(),
      price: this.product.price,
      category: this.product.category.trim()
    };

    console.log('Envoi du produit:', productToSend);

    if (this.isEditMode && this.product.id) {
      this.productService.updateProduct(this.product.id, productToSend).subscribe({
        next: (result) => {
          console.log('Produit mis à jour:', result);
          this.router.navigate(['/products']);
        },
        error: (err) => {
          console.error('Erreur détaillée:', err);
          this.error = `Erreur lors de la mise à jour: ${err.message || err.statusText || 'Erreur inconnue'}`;
          this.loading = false;
        }
      });
    } else {
      this.productService.createProduct(productToSend).subscribe({
        next: (result) => {
          console.log('Produit créé:', result);
          this.router.navigate(['/products']);
        },
        error: (err) => {
          console.error('Erreur détaillée:', err);
          this.error = `Erreur lors de la création: ${err.message || err.statusText || 'Erreur inconnue'}`;
          this.loading = false;
        }
      });
    }
  }

  isFormValid(): boolean {
    return !!(this.product.name && this.product.name.trim() && 
              this.product.price > 0 && 
              this.product.category && this.product.category.trim());
  }

  cancel(): void {
    this.router.navigate(['/products']);
  }
}

