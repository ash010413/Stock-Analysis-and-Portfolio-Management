import { Component, OnInit } from '@angular/core';
import * as Highcharts from 'highcharts/highstock';
import { Options, SeriesOptionsType } from 'highcharts';
import { CompanydataService } from '../companydata.service';


@Component({
  selector: 'app-recommendation-charts',
  templateUrl: './recommendation-charts.component.html',
  styleUrls: ['./recommendation-charts.component.css']
})

export class RecommendationChartsComponent implements OnInit {

  constructor(private companyDataService: CompanydataService) {}

  Highcharts: typeof Highcharts = Highcharts;
  chartOptions!: Highcharts.Options;
  ngOnInit() : void {
    this.companyDataService.mergedCompanyData$.subscribe(data => {
      console.log('Inside CompanyDataService Call in charts.ts');
      const recoData : any = {
        buy: [],
        hold: [],
        period: [],
        sell: [],
        strongBuy: [],
        strongSell: [],
        symbol: []
      };

      data.recommendation.forEach((recommendation: any) => {
        recoData.buy.push(recommendation.buy);
        recoData.hold.push(recommendation.hold);
        recoData.period.push(recommendation.period);
        recoData.sell.push(recommendation.sell);
        recoData.strongBuy.push(recommendation.strongBuy);
        recoData.strongSell.push(recommendation.strongSell);
        recoData.symbol.push(recommendation.symbol);
      });
      console.log('Recommendation data: ', recoData);
      this.chartOptions = {
        chart: {
          type: 'column',
          backgroundColor: '#f0f0f0',
          events: {
            load: function () {
              window.addEventListener('resize', () => {
                this.reflow();
              });
            }
          }
        },
        title: {
          text: 'Recommendation Trends',
          align: 'center'
        },
        xAxis: {
          categories: recoData.period
        },
        yAxis: {
          min: 0,
          title: {
            text: '#Analysis'
          }
          // stackLabels: {
          //   enabled: true
          // }
        },
        legend: {
          // align: 'center',
          verticalAlign: 'bottom',
          y: 10,
          // floating: true,
          backgroundColor: Highcharts.defaultOptions.legend ? Highcharts.defaultOptions.legend.backgroundColor || 'white' : 'white',
          // borderColor: '#CCC',
          // borderWidth: 1,
          shadow: false
        },
        tooltip: {
          headerFormat: '<b>{point.x}</b><br/>',
          pointFormat: '{series.name}: {point.y}<br/>Total: {point.stackTotal}'
        },
        plotOptions: {
          column: {
            stacking: 'normal',
            dataLabels: {
              enabled: true
            }
          }
        },
        series: [{
          name: 'strongBuy',
          data: recoData.strongBuy,
          color: '#176333'
        } as SeriesOptionsType, {
          name: 'Buy',
          data: recoData.buy,
          color: '#25af51'
        } as SeriesOptionsType, {
          name: 'Hold',
          data: recoData.hold,
          color: '#ae7e27'
        } as SeriesOptionsType, {
          name: 'Sell',
          data: recoData.sell,
          color: '#f15053'
        } as SeriesOptionsType, {
          name: 'Strong Sell',
          data: recoData.strongSell,
          color: '#752b2c'
        } as SeriesOptionsType]
      };
    })
  }
}

