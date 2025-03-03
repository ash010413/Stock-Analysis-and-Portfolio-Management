function initChart() {
    const historicalData = JSON.parse(Android.getHistoricalData());
    console.log(historicalData);
    const ticker = Android.getTicker(); // Get the ticker value from Android

    const ohlc = [];
    const volume = [];

    if (Array.isArray(historicalData)) {
        for (let i = 0; i < historicalData.length; i++) {
            const result = historicalData[i];
            const timestamp = result.t; // UNIX timestamp
            const open = result.o;
            const high = result.h;
            const low = result.l;
            const close = result.c;
            const vol = result.v;
            ohlc.push([timestamp, open, high, low, close]); // Add OHLC data
            volume.push([timestamp, vol]); // Add volume data
        }
    } else {
        console.error('Historical data is not an array');
        return; // Exit the function if historicalData is not an array
    }

    const groupingUnits = [
        ['day', [1]],     // Group by days
        ['week', [1]],    // Group by weeks
        ['month', [1, 3, 6]],  // Group by months, showing every 1st, 3rd, and 6th month
        ['year', null]    // Group by years
    ];

    const options = {
        chart: {
            backgroundColor:  '#FFFFFF',
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
        rangeSelector: {
            allButtonsEnabled: true,
            enabled: true,
            inputEnabled: true,
            buttons: [{
                type: 'month',
                count: 1,
                text: '1m'
            }, {
                type: 'month',
                count: 3,
                text: '3m'
            }, {
                type: 'month',
                count: 6,
                text: '6m'
            }, {
                type: 'ytd',
                text: 'YTD'
            }, {
                type: 'year',
                count: 1,
                text: '1y'
            }, {
                type: 'all',
                text: 'All'
            }],
            selected: 2
        },
        title: {
            text: `${ticker} Historical` // Use ticker instead of this.symbol
        },
        subtitle: {
            text: 'With SMA and Volume by Price technical indicators'
        },
        xAxis: {
            type: 'datetime',
            labels: {
                formatter: function() {
                    const date = new Date(this.value);
                    const day = date.getDate();
                    const month = date.toLocaleString('default', { month: 'short' });
                    return day + ' ' + month;
                }
            }
        },
        yAxis: [{
            startOnTick: false,
            endOnTick: false,
            labels: {
                align: 'right',
                x: -3
            },
            title: {
                text: 'OHLC'
            },
            height: '60%',
            lineWidth: 2,
            resize: {
                enabled: true
            },
            opposite: true
        }, {
            labels: {
                align: 'right',
                x: -3
            },
            title: {
                text: 'Volume'
            },
            top: '65%',
            height: '35%',
            offset: 0,
            lineWidth: 2,
            opposite: true
        }],
        tooltip: {
            split: true
        },
        plotOptions: {
            series: {
                dataGrouping: {
                    units: groupingUnits
                }
            }
        },
        series: [{
            type: 'candlestick',
            name: ticker, // Use ticker instead of this.symbol
            id: ticker, // Use ticker instead of this.symbol
            zIndex: 2,
            data: ohlc
        }, {
            type: 'column',
            name: 'Volume',
            id: 'volume',
            data: volume,
            yAxis: 1
        }, {
            type: 'vbp',
            linkedTo: ticker, // Use ticker instead of this.symbol
            params: {
                volumeSeriesID: 'volume'
            },
            dataLabels: {
                enabled: false
            },
            zoneLines: {
                enabled: false
            }
        }, {
            type: 'sma',
            linkedTo: ticker, // Use ticker instead of this.symbol
            zIndex: 1,
            marker: {
                enabled: false
            }
        }],
        navigator: {
            enabled: true
        }
    };

    // Render Highcharts chart
    Highcharts.chart('container', options);
}
