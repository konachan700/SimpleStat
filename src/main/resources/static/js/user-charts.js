var labels = [];
var dataTX = [];
var dataRX = [];
for (var i = 0; i < 60; i++) {
    labels[i] = i;
    dataTX[i] = 0;
    dataRX[i] = 0;
}
const ctx = document.getElementById('networkChart');
const totalNetworkChart = new Chart(ctx, {
    type: 'line',
    data: {
        labels: labels,
        datasets: [
            {
                label: 'Network TX',
                data: dataTX,
                fill: false,
                borderColor: 'rgb(255, 90, 90)',
                tension: 0.1,
                borderWidth : 1
            },
            {
                label: 'Network RX',
                data: dataRX,
                fill: false,
                borderColor: 'rgb(90, 255, 90)',
                tension: 0.1,
                borderWidth : 1
            }
        ]
    },
    options: {
        scales: {
            y: {
                beginAtZero: true
            }
        },
        elements: {
            point:{
                radius: 0
            }
        }
    },
    plugins: [
        {
            id: 'custom_canvas_background_color',
            beforeDraw: (chart) => {
                const {ctx} = chart;
                ctx.save();
                ctx.globalCompositeOperation = 'destination-over';
                ctx.fillStyle = 'rgb(50,50,50)';
                ctx.fillRect(0, 0, chart.width, chart.height);
                ctx.restore();
            }
        }
    ]
});

totalNetworkChart.options.plugins.legend.labels.color = 'rgba(222, 222, 222, 0.9)';
totalNetworkChart.options.animation = false; // disables all animations
totalNetworkChart.options.animations.colors = false; // disables animation defined by the collection of 'colors' properties
totalNetworkChart.options.animations.x = false; // disables animation defined by the 'x' property
totalNetworkChart.options.transitions.active.animation.duration = 0; // disables the animation for 'active' mode

function totalNetworkChartUpdate(json) {
    totalNetworkChart.data.datasets[0].label = "RX: " + __humanReadable(json.netTotalInBytesPerSecond) + "/sec";
    totalNetworkChart.data.datasets[1].label = "TX: " + __humanReadable(json.netTotalOutBytesPerSecond) + "/sec";
    totalNetworkChart.data.datasets[0].data = json.networkLastMinuteStatInPercentage;
    totalNetworkChart.data.datasets[1].data = json.networkLastMinuteStatOutPercentage;
    totalNetworkChart.update();
}

const ctx2 = document.getElementById('cpuChart');
const totalCPUChart = new Chart(ctx2, {
    type: 'line',
    data: {
        labels: labels,
        datasets: [
            {
                label: 'CPU',
                data: dataTX,
                fill: false,
                borderColor: 'rgb(255, 90, 90)',
                tension: 0.1,
                borderWidth : 1
            }
        ]
    },
    options: {
        scales: {
            y: {
                beginAtZero: true
            }
        },
        elements: {
            point:{
                radius: 0
            }
        }
    },
    plugins: [
        {
            id: 'custom_canvas_background_color',
            beforeDraw: (chart) => {
                const {ctx} = chart;
                ctx.save();
                ctx.globalCompositeOperation = 'destination-over';
                ctx.fillStyle = 'rgb(50,50,50)';
                ctx.fillRect(0, 0, chart.width, chart.height);
                ctx.restore();
            }
        }
    ]
});
totalCPUChart.options.plugins.legend.labels.color = 'rgba(222, 222, 222, 0.9)';
totalCPUChart.options.animation = false; // disables all animations
totalCPUChart.options.animations.colors = false; // disables animation defined by the collection of 'colors' properties
totalCPUChart.options.animations.x = false; // disables animation defined by the 'x' property
totalCPUChart.options.transitions.active.animation.duration = 0; // disables the animation for 'active' mode

function totalCPUChartUpdate(json) {
    totalCPUChart.data.datasets.length = 0;
    var arr = [];
    var cnt = 0;
    Object.keys(json.cpuLastLoadPerMinute).forEach(function (cpuData) { arr[cnt++] = cpuData; });
    arr.sort();
    arr.forEach(function (cpuData) {
        var colorRed   = (cpuData === "cpu" ? 0 : __randomX(cpuData, 2));
        var colorGreen = (cpuData === "cpu" ? 255 : 0);
        var colorBlue  = (cpuData === "cpu" ? 0 : __randomX(cpuData, 3));
        totalCPUChart.data.datasets[totalCPUChart.data.datasets.length] = {
            label: cpuData,
            data: json.cpuLastLoadPerMinute[cpuData],
            fill: false,
            borderColor: 'rgb(' + colorRed + ', ' + colorGreen + ', ' + colorBlue + ')',
            tension: 0.1,
            borderWidth : (cpuData === "cpu" ? 2 : 1)
        };
    });
    totalCPUChart.update();
    $("#cpu_load_title").html("Total CPU usage is " + json.cpuLoadTotal + "%")
}

var __colors = []
function __randomX(col, num) {
    if (!__colors[col + "__" + num]) {
        __colors[col + "__" + num] = Math.round(Math.random() * 200 + 55);
    }
    return __colors[col + "__" + num];
}