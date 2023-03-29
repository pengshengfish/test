/** axios封装
 * 请求拦截、相应拦截、错误统一处理
 */

let element_request = axios.create({
    baseURL: './',
    responseType: 'json',
    validateStatus (status) {
        // 200 外的状态码都认定为失败
        return status === 200
    }
});

// 拦截请求
element_request.interceptors.request.use((config) => {
    return config
}, (error) => {
    return Promise.reject(error)
});

// 拦截响应
element_request.interceptors.response.use((res) => {
    return res
}, (error) => {
    debugger
    if (error.response) {
        debugger
        let errorMessage = error.response.data === null ? '系统内部异常，请联系网站管理员' : error.response.data.message
        switch (error.response.status) {
            case 404:
                // notification.error({
                //     message: '系统提示',
                //     description: '很抱歉，资源未找到',
                //     duration: 4
                // })
                break
            case 403:
                var win = window;
                while (win != win.top){
                    win = win.top;
                }
                win.location.href= error.response.headers.contentpath;
                break
            case 401:
                // notification.warn({
                //     message: '系统提示',
                //     description: '很抱歉，您无法访问该资源，可能是因为没有相应权限或者登录已失效',
                //     duration: 4
                // })
                break
            default:
                // notification.error({
                //     message: '系统提示',
                //     description: errorMessage,
                //     duration: 4
                // })
                break
        }
    }
    return Promise.reject(error)
});

const request = {
    post (url, params) {
        return element_request.post(url, params, {
            headers: {
                'Content-Type': 'application/json'
            }
        })
    },
    put (url, params) {
        return element_request.put(url, params, {
            transformRequest: [(params) => {
                let result = ''
                Object.keys(params).forEach((key) => {
                    if (!Object.is(params[key], undefined) && !Object.is(params[key], null)) {
                        result += encodeURIComponent(key) + '=' + encodeURIComponent(params[key]) + '&'
                    }
                })
                return result
            }],
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            }
        })
    },
    get (url, params) {
        let _params
        if (Object.is(params, undefined)) {
            _params = ''
        } else {
            _params = '?'
            for (let key in params) {
                if (params.hasOwnProperty(key) && params[key] !== null) {
                    _params += `${key}=${params[key]}&`
                }
            }
        }
        return element_request.get(`${url}${_params}`)
    },
    delete (url, params) {
        let _params
        if (Object.is(params, undefined)) {
            _params = ''
        } else {
            _params = '?'
            for (let key in params) {
                if (params.hasOwnProperty(key) && params[key] !== null) {
                    _params += `${key}=${params[key]}&`
                }
            }
        }
        return element_request.delete(`${url}${_params}`)
    },
}

function getUrlParam() {
    var url = window.location.href;//获取当前url
    var dz_url = url.split('#')[0];//获取#/之前的字符串
    var cs = dz_url.split('?')[1];//获取?之后的参数字符串
    var cs_arr = cs.indexOf('&')>0?cs.split('&'):cs;//参数字符串分割为数组
    var param = {};
    for (var i = 0; i < cs_arr.length; i++) {//遍历数组，拿到json对象
        param[cs_arr[i].split('=')[0]] = cs_arr[i].split('=')[1]
    }
    return param;
}

function geturl(name,iframeId) {
    var reg = new RegExp("[^\?&]?" + encodeURI(name) + "=[^&]+");
    var arr = window.parent.document.getElementById(iframeId).contentWindow.location.search.match(reg);
    if (arr != null) {
        return decodeURI(arr[0].substring(arr[0].search("=") + 1));
    }
    return "";
}