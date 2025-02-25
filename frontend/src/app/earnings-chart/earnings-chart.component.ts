import { Component, OnInit } from '@angular/core';
import * as Highcharts from 'highcharts/highstock';
import { Options, SeriesOptionsType } from 'highcharts';
import { CompanydataService } from '../companydata.service';
import { SeriesLineOptions } from 'highcharts';

@Component({
  selector: 'app-earnings-chart',
  templateUrl: './earnings-chart.component.html',
  styleUrls: ['./earnings-chart.component.css']
})

export class EarningsChartComponent implements OnInit {
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
        return `${period}<br>Surprise: ${surpriseValue}`;
    })

      this.chartOptions = {
        chart: {
          type: 'spline',
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
            },
            style: {
              fontSize: '12px'
            }
          },
        },
        yAxis: {
          min: 0,
          title: {
            text: 'Quarterly EPS',
          },
          stackLabels: {
            enabled: true
          }
        },
        legend: {
          verticalAlign: 'bottom',
          y: 10,
          shadow: false
        },
        tooltip: {
          headerFormat: '<b>{series.name}</b><br/>',
          pointFormat: '{point.x}: {point.y:.2f}',
        },
        plotOptions: {
          column: {
            stacking: 'normal',
            dataLabels: {
              enabled: true
            }
          }
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
