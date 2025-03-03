function initializePriceChart() {
    console.log('Initializing price chart...');

    const hourlyDataString = Android.getHourlyData();
    const hourlyData = JSON.parse(hourlyDataString);
    const changeValue = Android.getChangeValue();
    const ticker = Android.getTicker();

    console.log('Hourly Data String:', hourlyDataString);
    console.log('Hourly Data:', hourlyData);
    console.log('Change Value:', changeValue);
    console.log('Ticker:', ticker);

    if (hourlyData && hourlyData.results) {
        console.log('Hourly data results are available:', hourlyData.results);
    } else {
        console.error('Hourly data results are not available.');
        return;
    }

    const timestamps = hourlyData.results.map(result => new Date(result.t).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: false }));
    const stockPrices = hourlyData.results.map(result => result.c);

    const data = timestamps.map((timestamp, index) => [timestamp, stockPrices[index]]);

    const totalEntries = timestamps.length;
    const stepSize = Math.ceil(totalEntries / 10);

    const lineColor = changeValue > 0 ? 'green' : changeValue < 0 ? 'red' : 'black';

    console.log('Timestamps:', timestamps);
    console.log('Stock Prices:', stockPrices);
    console.log('Data:', data);
    console.log('Total Entries:', totalEntries);
    console.log('Step Size:', stepSize);
    console.log('Line Color:', lineColor);

    const priceChartOptions = {
        chart: {
            type: 'line',
            backgroundColor: '#FFFFFF'
        },
        accessibility: {
            enabled: false
        },
        scrollbar: {
            enabled: true
        },
        legend: {
            enabled: false
        },
        title: {
            text: ticker + ' Hourly Price Variation'
        },
        xAxis: {
            categories: timestamps,
            tickInterval: stepSize,
            labels: {
                formatter: function () {
                    return this.value.toString();
                }
            },
            tickWidth: 2,
            tickLength: 10
        },
        yAxis: {
            title: {
                text: null
            },
            opposite: true
        },
        series: [{
            name: '',
            type: 'line',
            color: lineColor,
            data: data,
            marker: {
                enabled: false
            }
        }]
    };
    Highcharts.chart('hourly-chart-container', priceChartOptions)
}