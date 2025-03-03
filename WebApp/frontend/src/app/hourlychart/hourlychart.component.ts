import { Component, OnInit } from '@angular/core';
import * as Highcharts from 'highcharts/highstock';
import { CompanydataService } from '../companydata.service';
import { SearchResultsService } from '../search-results.service';
import { SeriesLineOptions } from 'highcharts';

@Component({
  selector: 'app-hourlychart',
  templateUrl: './hourlychart.component.html',
  styleUrls: ['./hourlychart.component.css']
})
export class HourlychartComponent implements OnInit {
  storedResults: any;

  constructor(private companyDataService: CompanydataService) {}

  Highcharts: typeof Highcharts = Highcharts;
  chartOptions!: Highcharts.Options;

  ngOnInit(): void {
    this.companyDataService.mergedCompanyData$.subscribe(data => {
      console.log('Inside CompanyDataService Call in earnings-charts.ts: ', data);
      const earningsData : any = {
        actual: [],
        estimate: [],
        period: [],
        symbol: [],
        surprise: [],
        surpirsePercent: []
      };
      data.earnings.forEach((earning: any) => {
        earningsData.actual.push(earning.actual);
        earningsData.estimate.push(earning.estimate);
        earningsData.period.push(earning.period);
        earningsData.symbol.push(earning.symbol);
        earningsData.surprise.push(earning.surprise);
        earningsData.surpirsePercent.push(earning.surprisePercent);
      });
      console.log('Earnings data: ', earningsData);

      const xAxisLabels = earningsData.period.map((period: string, index: number) => {
        const surpriseValue = earningsData.surprise[index];
        return `${period}\nSurprise: ${surpriseValue}`;
    })

      this.chartOptions = {
        chart: {
          type: 'spline',
        },
        title: {
          text: 'Historical EPS Surprises',
          align: 'center',
        },
        xAxis: {
          title: {
            text: 'Quarterly EPS',
          },
          labels: {
            format: '{value}',
            formatter: function () {
              return xAxisLabels[this.pos];
            }
          },
        },
        yAxis: {
          title: {
            text: 'Quarterly EPS',
          },
        },
        tooltip: {
          headerFormat: '<b>{series.name}</b><br/>',
          pointFormat: '{point.x}: {point.y:.2f}',
        },
        series: [
          {
            name: 'Actual',
            data: earningsData.actual,
          } as SeriesLineOptions,
          {
            name: 'Estimate',
            data: earningsData.estimate
          } as SeriesLineOptions
        ],
      };
  });
  }

}

