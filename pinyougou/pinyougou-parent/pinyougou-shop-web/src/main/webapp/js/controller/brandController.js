app.controller("brandController", function ($scope,$controller, brandService) {
    $controller('baseController',{$scope:$scope});
    //查询所有数据
    $scope.findAll = function () {
        brandService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    }


    //查询
    $scope.findPage = function (page, rows) {
        brandService.findpage(page,rows).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;
            }
        )
    }
    //保存
    $scope.save = function () {
        var object = null;
        if ($scope.entity.id != null) {
            object = brandService.update($scope.entity);
        }else{
            object= brandService.add($scope.entity);
        }
        object.success(
            function (response) {
                if (response.success) {
                    //重新查询
                    $scope.reloadList();
                } else {
                    alert(response.message)
                }
            }
        )
    }
    //修改查询1
    $scope.findOne = function (id) {
        brandService.findOne(id).success(
            function (response) {
                $scope.entity = response;
            }
        )
    }

    //2.进行删除操作
    $scope.dele = function () {
        brandService.dele($scope.selectIds).success(
            function (response) {
                if (response.success) {
                    //重新查询
                    $scope.reloadList();
                } else {
                    alert(response.message)
                }
            }
        )
    }
    //根据要求查询
    $scope.searchEntity = {};
    $scope.search = function (page, rows) {
        brandService.search(page,rows,$scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;//给列表变量赋值
                $scope.paginationConf.totalItems = response.total;//总记录数
            }
        )
    }
    //全选
    $scope.selectAll=function ($event) {
        $scope.selectIds=[];
        var isAllchecked=$event.target.checked;
        if(isAllchecked){
            for(var i=0;i<$scope.list.length;i++){
                $scope.selectIds.push($scope.list[i].id);
            }
        }
    }

});