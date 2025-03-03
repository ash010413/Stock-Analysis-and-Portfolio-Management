import { Component, Input, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { HomeComponent } from '../home/home.component';
import { HttpClient } from '@angular/common/http';
import { PortfolioService } from '../portfolio.service';
import { PortfolioComponent } from '../portfolio/portfolio.component';
import { Router} from '@angular/router';
import { SellbuttonclickedService } from '../sellbuttonclicked.service';
import { BuybuttonclickedService } from '../buybuttonclicked.service';


@Component({
  selector: 'app-portfoliosellmodal',
  templateUrl: './portfoliosellmodal.component.html',
  styleUrls: ['./portfoliosellmodal.component.css']
})
export class PortfoliosellmodalComponent implements OnInit {
  @Input() symbol!: string;
  @Input() currentPrice!: number;
  @Input() balanceWallet!: number;
  @Input() quantity!: number;
  @Input() oldQuantity!: number;

  constructor(public buyButtonclickedService: BuybuttonclickedService, public sellbuttonClickedService : SellbuttonclickedService,public activeModal: NgbActiveModal, private http: HttpClient, private homeComponent: HomeComponent, private router: Router, private portfolioService: PortfolioService, private portfolioComponent: PortfolioComponent) { }

  portfolioData: any[] = [];

  ngOnInit(): void {
    this.sellbuttonClickedService.sellButtonClicked = false;
  }

  SelfClosingAlertforSell(symbol:string) {
    console.log('SelfClosingAlertforSell');
    this.portfolioComponent.alertType = 'danger';
    this.portfolioComponent.statusMessage = `${symbol} sold successfully.`;
    setTimeout(() => {
      this.portfolioComponent.statusMessage = '';
    }, 5000);
  }

  sell(): void {
    const newBalance = this.balanceWallet + (this.currentPrice * this.quantity);
    this.http.post<any>('http://localhost:3000/update-wallet', { newBalance }).subscribe({
      next: () => {
        console.log('Wallet updated successfully');
        this.sellbuttonClickedService.sellButtonClicked = true;
        this.closeModal();
        this.sellPortfolioUpdate();
        this.buyButtonclickedService.buyButtonClicked = false;
        // this.sellbuttonClickedService.sellButtonClicked = false;
      },
      error: (error) => {
        console.error('Error updating wallet:', error);
      }
    });

    // this.http.post<any>('/update-wallet', { newBalance }).subscribe({
    //   next: () => {
    //     console.log('Wallet updated successfully');
    //     this.sellbuttonClickedService.sellButtonClicked = true;
    //     this.closeModal();
    //     this.sellPortfolioUpdate();
    //     // this.sellbuttonClickedService.sellButtonClicked = false;
    //   },
    //   error: (error) => {
    //     console.error('Error updating wallet:', error);
    //   }
    // });
  }

  sellPortfolioUpdate() : void {
    const data = {
      symbol: this.symbol,
      companyName: this.homeComponent.company?.name,
      quantity: this.quantity,
      currentPrice: this.currentPrice
    };

    this.http.post<any>('http://localhost:3000/sell-from-portfolio', data).subscribe({
      next: () => {
        console.log('Selling stocks updated in Portfolio!');
      },
      error: (error) => {
        console.error('Error updating data of selling in portfolio', error);
      }
    });


    // this.http.post<any>('/sell-from-portfolio', data).subscribe({
    //   next: () => {
    //     console.log('Selling stocks updated in Portfolio!');
    //   },
    //   error: (error) => {
    //     console.error('Error updating data of selling in portfolio', error);
    //   }
    // });
  }

  closeModal() {
    // this.fetchWalletBalance();
    this.portfolioService.getPortfolioData().subscribe(data => {
      console.log(data);
      this.portfolioData = data;
    });
  this.activeModal.dismiss();
  this.activeModal.close();
  }

}

