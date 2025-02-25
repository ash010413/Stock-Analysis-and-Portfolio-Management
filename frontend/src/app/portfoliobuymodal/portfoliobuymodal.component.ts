import { Component, Input } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { HomeComponent } from '../home/home.component';
import { HttpClient } from '@angular/common/http';
import { PortfolioService } from '../portfolio.service';
import { PortfolioComponent } from '../portfolio/portfolio.component';
import { PortfolioSharedService } from '../portfolio-shared-service.service';
import { BuybuttonclickedService } from '../buybuttonclicked.service'


@Component({
  selector: 'app-portfoliobuymodal',
  templateUrl: './portfoliobuymodal.component.html',
  styleUrls: ['./portfoliobuymodal.component.css']
})
export class PortfoliobuymodalComponent {
  @Input() symbol!: string;
  @Input() currentPrice!: number;
  @Input() balanceWallet!: number;
  @Input() quantity: number = 0;

  constructor(private buybuttonclickedService : BuybuttonclickedService,private portfolioSharedService: PortfolioSharedService, public activeModal: NgbActiveModal, private http: HttpClient, private homeComponent: HomeComponent, private portfolioService: PortfolioService, private portfolioComponent: PortfolioComponent) { }

  portfolioData: any[] = [];
  buyOrNot: boolean = false;
  
buy(): void {
  console.log('Inside Buy function');
  this.buyOrNot = true;
  this.portfolioSharedService.setBuyOrNot(true);
    const newBalance = this.balanceWallet - (this.currentPrice * this.quantity);
    this.http.post<any>('http://localhost:3000/update-wallet', { newBalance }).subscribe({
      next: () => {
        console.log('Wallet updated successfully');
        this.buybuttonclickedService.buyButtonClicked = true;
        this.closeModal();
        this.addToPortfolio();
        this.buybuttonclickedService.buyButtonClicked = false;
      },
      error: (error) => {
        console.error('Error updating wallet:', error);
      }
    });

    // this.http.post<any>('/update-wallet', { newBalance }).subscribe({
    //   next: () => {
    //     console.log('Wallet updated successfully');
    //     this.buybuttonclickedService.buyButtonClicked = true;
    //     this.closeModal();
    //     this.addToPortfolio();
    //     this.buybuttonclickedService.buyButtonClicked = false;
    //   },
    //   error: (error) => {
    //     console.error('Error updating wallet:', error);
    //   }
    // });
  }

  SelfClosingAlertforBuy(symbol:string) {
    console.log('SelfClosingALertforBuy');
    this.portfolioComponent.alertType = 'success';
    this.portfolioComponent.statusMessage = `${symbol} is bought successfully.`;
    setTimeout(() => {
      this.portfolioComponent.statusMessage = '';
    }, 5000);
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
    // this.fetchWalletBalance();
    this.portfolioService.getPortfolioData().subscribe(data => {
      this.portfolioData = data;
    });
  this.activeModal.dismiss();
  this.activeModal.close();
  }

}
