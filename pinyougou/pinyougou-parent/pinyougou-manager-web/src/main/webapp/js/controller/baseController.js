app.controller('baseController',function ($scope) {
    // 刷新
    $scope.reloadList = function () {
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    }
    //分页条件
    $scope.paginationConf = {
        currentPage: 1,
        totalItems: 10,
        itemsPerPage: 10,
        perPageOptions: [10, 20, 30, 40, 50],
        onChange: function () {
            $scope.reloadList();//重新加载
        }
    }
    //删除
    $scope.selectIds = [];
    //1.创建一个数组使用户选择的id放到集合里
    $scope.updateSelection = function (id, $event) {
        if ($event.target.checked) {
            $scope.selectIds.push(id);
        } else {
            var idx = $scope.selectIds.indexOf(id);
            $scope.selectIds.splice(idx, 1);
        }
    }
    //提取 json 字符串数据中某个属性，返回拼接字符串 逗号分隔
    $scope.jsonToString=function(jsonString,key){
        var json=JSON.parse(jsonString);//将 json 字符串转换为 json 对象
        var value="";
        for(var i=0;i<json.length;i++){
            if(i>0){
                value+=","
            }
            value+=json[i][key];
        }
        return value;
    }

});