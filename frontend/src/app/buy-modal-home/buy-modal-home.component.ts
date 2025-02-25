import { Component, Input} from '@angular/core';
import { NgbActiveModal, NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { HttpClient } from '@angular/common/http';
import { HomeComponent } from '../home/home.component';
import { PortfolioComponent } from '../portfolio/portfolio.component';

@Component({
  selector: 'app-buy-modal-home',
  templateUrl: './buy-modal-home.component.html',
  styleUrls: ['./buy-modal-home.component.css']
})
export class BuyModalHomeComponent {
  @Input() symbol!: string;
  @Input() currentPrice!: number;
  @Input() balanceWallet!: number;
  @Input() quantity: number = 0;

  constructor(public activeModal: NgbActiveModal, private http: HttpClient, private homeComponent: HomeComponent, private portfolioComponent: PortfolioComponent) { }

  SelfClosingAlertforBuy(symbol:string) {
    console.log('SelfClosingALertforBuy');
    this.portfolioComponent.alertType = 'success';
    this.portfolioComponent.statusMessage = `${symbol} is bought successfully.`;
    setTimeout(() => {
      this.portfolioComponent.statusMessage = '';
    }, 5000);
  }

  buy(): void {
    const newBalance = this.balanceWallet - (this.currentPrice * this.quantity);

    this.http.post<any>('http://localhost:3000/update-wallet', { newBalance }).subscribe({
      next: () => {
        console.log('Wallet updated successfully');
        this.closeModal();
        this.addToPortfolio();
      },
      error: (error) => {
        console.error('Error updating wallet:', error);
      }
    });

    // this.http.post<any>('/update-wallet', { newBalance }).subscribe({
    //   next: () => {
    //     console.log('Wallet updated successfully');
    //     this.closeModal();
    //     this.addToPortfolio();
    //   },
    //   error: (error) => {
    //     console.error('Error updating wallet:', error);
    //   }
    // });
  }

  addToPortfolio(): void {
    const data = {
      symbol: this.symbol,
      companyName: this.homeComponent.company?.name,
      quantity: this.quantity,
      total: this.currentPrice * this.quantity,
      currentPrice: this.currentPrice
    };

    this.http.post<any>('http://localhost:3000/add-to-portfolio', data).subscribe({
      next: () => {
        console.log('Data added to portfolio successfully');
      },
      error: (error) => {
        console.error('Error adding data to portfolio:', error);
      }
    });

    // this.http.post<any>('/add-to-portfolio', data).subscribe({
    //   next: () => {
    //     console.log('Data added to portfolio successfully');
    //   },
    //   error: (error) => {
    //     console.error('Error adding data to portfolio:', error);
    //   }
    // });
  }

  closeModal() {
    this.activeModal.close();
  }

}
