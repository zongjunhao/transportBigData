// 小区划分
function draw_zone() {


    $.get('/stay/getZoneCenter', function (data) {
        var map = new AMap.Map('zone-zone', {
            mapStyle: 'amap://styles/whitesmoke',
            zoom: 10,
            center: [123.2531, 41.8011],
            features: ['bg', 'road'],
            eventSupport: true,  // 图层事件支持，LabelsLayer 默认开启
            fitView: true,
            visible: true,
            zIndex: 99,
            collision: false,  // 是否开启文字自动避让
            resizeEnable: true
        });
        // 点图层
        var pointLayer = new Loca.PointLayer({
            map: map
        });

        pointLayer.setData(data.data, {
            lnglat: function (obj) {
                var value = obj.value;
                var lnglat = [value['longitude'], value['latitude']];
                // console.log(lnglat, new Date().getMilliseconds(), obj.index, value['timestamp'])
                return lnglat;
            },
        }).setOptions({
            style: {
                // 圆形半径，单位像素
                radius: 6,
                // 填充颜色
                // color: function (data) {
                //     var value = data.value;
                //     // "index" + value[indexedDB] +
                //     // return color[value['cluster'] - 1]
                //     let array = value['laci'].split('-');
                //     let num =  array[0];
                //     // return `rgb(1${num[2]}0,1${num[3]}0,1${num[4]}0)`
                //     return `#${num[2]}${num[2]}${num[3]}${num[3]}${num[4]}${num[4]}`
                // },
                color: '#15A3FA',

                // 描边颜色
                borderColor: '#15A3FA',
                // 描边宽度，单位像素
                borderWidth: 1,
                // 透明度 [0-1]
                opacity: 1,
            }
        }).render();

        // 文字标记图层
        var labelsLayer = new Loca.LabelsLayer({
            zooms: [3, 20],
            zIndex: 1000,
            // 开启标注避让，默认为开启，v1.4.15 新增属性
            collision: true,
            // 开启标注淡入动画，默认为开启，v1.4.15 新增属性
            // animation: true,
            map: map,
        });

        labelsLayer.setData(data.data, {
            lnglat: function (obj) {
                var value = obj.value;
                var lnglat = [value['longitude'], value['latitude']];
                return lnglat;
            },
        }).setOptions({
            style: {
                direction: "top",  // 文字位置
                offset: [0, 0],  // 文字偏移距离
                zooms: [3, 18],  // 文字显示范围
                text: function (data) {
                    var value = data.value;
                    // "index" + value[indexedDB] +
                    // return value['timestamp'] + " cluster" + value['cluster']
                    return value['zone']
                },  // 文本内容
                // fillColor: function () {
                //     return `rgb(${Math.random() * 255},${Math.random() * 255},${Math.random() * 255})`
                // },  // 文字填充色
                // fontFamily: '字体',  // 文字字体(2D)
                // fontSize: 30,  // 文字大小, 默认值：12
                fontWeight: "normal",  // 文字粗细(2D)。 可选值： 'normal'| 'lighter'| 'bold' 。默认值：'normal'
                strokeColor: "rgba(255,255,255,0.85)",  // 文字描边颜色
                strokeWidth: 1,  // 文字描边宽度
            }
        }).render();
        // map.setFitView()
    });
}

// 小区流入流出
function draw_inout_map(zone_id) {
    // 小区流出
    $.get("http://localhost:8090/stay/getOd1?startZone=" + zone_id, function (response) {
        console.log("out data", response.data)
        let response_data = response.data

        let pointLayer
        let labelsLayer

        let map = new AMap.Map('out-container', {
            mapStyle: 'amap://styles/whitesmoke',
            zoom: 13,
            center: [123.4231, 41.8011],
            features: ['bg', 'road'],
            eventSupport: true,  // 图层事件支持，LabelsLayer 默认开启
            fitView: true,
            visible: true,
            zIndex: 99,
            collision: false  // 是否开启文字自动避让
        });
        // 点图层
        pointLayer = new Loca.PointLayer({
            map: map
        });

        pointLayer.setData(response_data, {
            lnglat: function (obj) {
                var value = obj.value.endPoint;
                var lnglat = [value['longitude'], value['latitude']];
                // console.log(lnglat, new Date().getMilliseconds(), obj.index, value['timestamp']);
                return lnglat;
            },
            // type: 'csv'
        }).setOptions({
            style: {
                // 圆形半径，单位像素
                radius: 6,
                // 填充颜色
                color: '#15A3FA',
                // 描边颜色
                borderColor: '#15A3FA',
                // 描边宽度，单位像素
                borderWidth: 1,
                // 透明度 [0-1]
                opacity: 1,
            }
        }).render();

        // 文字标记图层
        labelsLayer = new Loca.LabelsLayer({
            zooms: [3, 20],
            zIndex: 1000,
            // 开启标注避让，默认为开启，v1.4.15 新增属性
            collision: true,
            // 开启标注淡入动画，默认为开启，v1.4.15 新增属性
            // animation: true,
            map: map,
        });

        labelsLayer.setData(response_data, {
            lnglat: function (obj) {
                let value = obj.value.endPoint;
                return [value['longitude'], value['latitude']];
            },
        }).setOptions({
            style: {
                direction: "top",  // 文字位置
                offset: [0, 0],  // 文字偏移距离
                zooms: [3, 18],  // 文字显示范围
                text: function (data) {
                    var value = data.value;
                    // "index" + value[indexedDB] +
                    return "end zone:" + value['endId'] + " count:" + value['count']
                },  // 文本内容
                // fillColor: function () {
                //     return `rgb(${Math.random() * 255},${Math.random() * 255},${Math.random() * 255})`
                // },  // 文字填充色
                // fontFamily: '字体',  // 文字字体(2D)
                // fontSize: 30,  // 文字大小, 默认值：12
                fontWeight: "normal",  // 文字粗细(2D)。 可选值： 'normal'| 'lighter'| 'bold' 。默认值：'normal'
                strokeColor: "rgba(255,255,255,0.85)",  // 文字描边颜色
                strokeWidth: 1,  // 文字描边宽度
            }
        }).render();

        for (let i = 0; i < response_data.length; i++) {
            let path = []
            let content = response_data[i].startPoint
            path.push([parseFloat(content.longitude), parseFloat(content.latitude)])
            content = response_data[i].endPoint
            path.push([parseFloat(content.longitude), parseFloat(content.latitude)])
            let polyline = new AMap.Polyline({
                path: path,
                borderWeight: 2, // 线条宽度，默认为 1
                strokeWeight: 6,
                strokeOpacity: 1,
                strokeColor: '#15A3FA',
                zIndex: 1000,
                showDir: true
            });
            map.add(polyline);
        }
        map.setFitView()
    })
    // 小区流入
    $.get("/stay/getOd1?endZone=" + zone_id, function (response) {
        console.log("in data", response.data)
        let response_data = response.data

        let pointLayer
        let labelsLayer

        let map = new AMap.Map('in-container', {
            mapStyle: 'amap://styles/whitesmoke',
            zoom: 13,
            center: [123.4231, 41.8011],
            features: ['bg', 'road'],
            eventSupport: true,  // 图层事件支持，LabelsLayer 默认开启
            fitView: true,
            visible: true,
            zIndex: 99,
            collision: false  // 是否开启文字自动避让
        });
        // 点图层
        pointLayer = new Loca.PointLayer({
            map: map
        });

        pointLayer.setData(response_data, {
            lnglat: function (obj) {
                var value = obj.value.startPoint;
                var lnglat = [value['longitude'], value['latitude']];
                // console.log(lnglat, new Date().getMilliseconds(), obj.index, value['timestamp']);
                return lnglat;
            },
            // type: 'csv'
        }).setOptions({
            style: {
                // 圆形半径，单位像素
                radius: 6,
                // 填充颜色
                color: '#15A3FA',
                // 描边颜色
                borderColor: '#15A3FA',
                // 描边宽度，单位像素
                borderWidth: 1,
                // 透明度 [0-1]
                opacity: 1,
            }
        }).render();

        // 文字标记图层
        labelsLayer = new Loca.LabelsLayer({
            zooms: [3, 20],
            zIndex: 1000,
            // 开启标注避让，默认为开启，v1.4.15 新增属性
            collision: true,
            // 开启标注淡入动画，默认为开启，v1.4.15 新增属性
            // animation: true,
            map: map,
        });

        labelsLayer.setData(response_data, {
            lnglat: function (obj) {
                let value = obj.value.startPoint;
                return [value['longitude'], value['latitude']];
            },
        }).setOptions({
            style: {
                direction: "top",  // 文字位置
                offset: [0, 0],  // 文字偏移距离
                zooms: [3, 18],  // 文字显示范围
                text: function (data) {
                    var value = data.value;
                    // "index" + value[indexedDB] +
                    return "start zone:" + value['startId'] + " count:" + value['count']
                },  // 文本内容
                // fillColor: function () {
                //     return `rgb(${Math.random() * 255},${Math.random() * 255},${Math.random() * 255})`
                // },  // 文字填充色
                // fontFamily: '字体',  // 文字字体(2D)
                // fontSize: 30,  // 文字大小, 默认值：12
                fontWeight: "normal",  // 文字粗细(2D)。 可选值： 'normal'| 'lighter'| 'bold' 。默认值：'normal'
                strokeColor: "rgba(255,255,255,0.85)",  // 文字描边颜色
                strokeWidth: 1,  // 文字描边宽度
            }
        }).render();

        for (let i = 0; i < response_data.length; i++) {
            let path = []
            let content = response_data[i].startPoint
            path.push([parseFloat(content.longitude), parseFloat(content.latitude)])
            content = response_data[i].endPoint
            path.push([parseFloat(content.longitude), parseFloat(content.latitude)])
            let polyline = new AMap.Polyline({
                path: path,
                borderWeight: 2, // 线条宽度，默认为 1
                strokeWeight: 6,
                strokeOpacity: 1,
                strokeColor: '#15A3FA',
                zIndex: 1000,
                showDir: true
            });
            map.add(polyline);
        }
        map.setFitView()
    })
}

// 初始化
function init() {
    draw_zone()
    draw_inout_map(0)
}

init()