import { Component, OnInit } from '@angular/core';
import * as Highcharts from 'highcharts/highstock';
import { Options, SeriesOptionsType } from 'highcharts';
import { CompanydataService } from '../companydata.service';
import { SeriesLineOptions } from 'highcharts';

@Component({
  selector: 'app-charts',
  templateUrl: './charts.component.html',
  styleUrls: ['./charts.component.css']
})
export class ChartsComponent implements OnInit {
  
  constructor(private companyDataService: CompanydataService) {}

  Highcharts: typeof Highcharts = Highcharts;
  chartOptions!: Highcharts.Options;

  ngOnInit(): void {
    this.companyDataService.mergedCompanyData$.subscribe(data => {
      console.log('Inside CompanyDataService Call in earnings-charts.ts: ', data);
      
    })
  }

}