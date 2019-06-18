//控制层
app.controller('goodsController', function ($scope, $controller, $location, goodsService, uploadService, itemCatService, typeTemplateService) {

    $controller('baseController', {$scope: $scope});//继承

    //读取列表数据绑定到表单中  
    $scope.findAll = function () {
        goodsService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    }

    //分页
    $scope.findPage = function (page, rows) {
        goodsService.findPage(page, rows).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //查询实体
    $scope.findOne = function () {
        var id = $location.search().id;
        if (id == null) {
            return;
        }
        goodsService.findOne(id).success(
            function (response) {
                $scope.entity = response;
                editor.html($scope.entity.goodsDesc.introduction);
                $scope.entity.goodsDesc.itemImages = JSON.parse($scope.entity.goodsDesc.itemImages);
                $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.entity.goodsDesc.customAttributeItems);
                $scope.entity.goodsDesc.specificationItems = JSON.parse($scope.entity.goodsDesc.specificationItems);
                for (var i = 0; i < $scope.entity.itemList.length; i++) {
                    $scope.entity.itemList[i].spec = JSON.parse($scope.entity.itemList[i].spec);

                }
            }
        );
    }

    // //保存
    // $scope.add = function () {
    //     $scope.entity.goodsDesc.introduction = editor.html();//将富文本内容提取到对象中,进行传递到后台数据
    //     goodsService.add($scope.entity).success(
    //         function (response) {
    //             if (response.success) {
    //                 alert("保存成功");
    //                 editor.html("");//清空富文本内容
    //                 $scope.entity = {};//清空商品的信息
    //             } else {
    //                 alert(response.message);
    //             }
    //         }
    //     );
    // }


    //批量删除
    $scope.dele = function () {
        //获取选中的复选框
        goodsService.dele($scope.selectIds).success(
            function (response) {
                if (response.success) {
                    $scope.reloadList();//刷新列表
                    $scope.selectIds = [];
                }
            }
        );
    }

    $scope.searchEntity = {};//定义搜索对象

    //搜索
    $scope.search = function (page, rows) {
        goodsService.search(page, rows, $scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }
    //上传文件方法
    $scope.uploadFile = function () {
        uploadService.uploadFile().success(function (response) {
                if (response.success) {
                    $scope.image_entity.url = response.message;
                } else {
                    alert(response.message);
                }
            }
        );
    }

    $scope.entity = {goods: {}, goodsDesc: {itemImages: [], specificationItems: [/*初始化规格列表*/]}};
    //添加图片列表
    $scope.add_image_entity = function () {
        $scope.entity.goodsDesc.itemImages.push($scope.image_entity);
    }//删除图片
    $scope.remove_image_entity = function (index) {
        $scope.entity.goodsDesc.itemImages.splice(index, 1)
    }
    //查找下拉框以及目录
    $scope.selectItemCat1List = function () {
        itemCatService.findByParentId(0).success(
            function (response) {
                $scope.itemCat1List = response;
            }
        );
    }
    //根据一级选择的ID查询二级数据
    $scope.$watch('entity.goods.category1Id', function (newValue, oldValue) {
        if (newValue == undefined || newValue == null || newValue == oldValue) {
            return;
        }
        itemCatService.findByParentId(newValue).success(
            function (response) {
                $scope.itemCat2List = response;
                $scope.itemCat3List = {};
            }
        );
    })
    //根据二级选择的ID查询三级数据
    $scope.$watch('entity.goods.category2Id', function (newValue, oldValue) {
        itemCatService.findByParentId(newValue).success(
            function (response) {
                $scope.itemCat3List = response;
            }
        );
    })
    //根据三级选择的ID查询模板表的数据
    $scope.$watch('entity.goods.category3Id', function (newValue, oldValue) {
        itemCatService.findOne(newValue).success(
            function (response) {
                $scope.entity.goods.typeTemplateId = response.typeId;
            }
        );
    })
    //根据模板ID查询品牌列表
    $scope.$watch('entity.goods.typeTemplateId', function (newValue, oldValue) {
        typeTemplateService.findOne(newValue).success(
            function (response) {
                $scope.typeTemplate = response;//获取类型模板
                $scope.typeTemplate.brandIds = JSON.parse($scope.typeTemplate.brandIds);//品牌列表
                if ($location.search()['id'] == null) {
                    $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.typeTemplate.customAttributeItems);//扩展属性
                }

            }
        );
        //查询规格列表
        typeTemplateService.findSpecList(newValue).success(
            function (response) {
                $scope.specList = response;
            }
        );
    })
    $scope.updateSpecAttribute = function ($event, name, value) {
        //将客户选择的规格以及规格列表的集合传给公共方法进行遍历
        var object = $scope.findObjectByKey($scope.entity.goodsDesc.specificationItems, "attributeName", name);
        //判断集合是否为空
        if (object != null) {
            //判断选择框是否被选中
            if ($event.target.checked) {
                //未被选中改的情况下把选择的值添加到集合中
                object.attributeValue.push(value);
            } else {
                //移除已被选中的元素
                object.attributeValue.splice(object.attributeValue.indexOf(value), 1);
                //如果选项都取消了的话,把整个记录移除掉
                if (object.attributeValue.length == 0) {
                    $scope.entity.goodsDesc.specificationItems.splice(
                        $scope.entity.goodsDesc.specificationItems.indexOf(object), 1);
                }
            }
            //如果选择框没有被选中,就添加一个元素到集合中
        } else {
            $scope.entity.goodsDesc.specificationItems.push({"attributeName": name, "attributeValue": [value]});
        }
    }
    $scope.createItemList = function () {
        //创建一个基础的集合对象
        // debugger
        $scope.entity.itemList = [{spec: {}, price: 0, num: 99999, status: '0', isDefault: '0'}];//定义驴打滚初始值
        var items = $scope.entity.goodsDesc.specificationItems;
        //循环遍历每一个规格名称
        for (var i = 0; i < items.length; i++) {
            $scope.entity.itemList = addColumn($scope.entity.itemList, items[i].attributeName, items[i].attributeValue);
        }
    }
    //添加新列
    addColumn = function (list, columnName, columnValues) {
        // debugger
        var newList = [];
        //遍历每一个老的列
        for (var i = 0; i < list.length; i++) {
            var oldRow = list[i];
            //把老的列添加新列到数组中
            for (var j = 0; j < columnValues.length; j++) {
                //深克隆一个集合对象
                var newRow = JSON.parse(JSON.stringify(oldRow));
                //将新的集合添加新的元素
                newRow.spec[columnName] = columnValues[j];
                newList.push(newRow);
            }
        }
        return newList;
    }
    $scope.status = ['未审核', '已审核', '审核未通过', '关闭'];//商品状态
    $scope.itemCatList = [];//商品分类列表
    //加载商品分类列表
    $scope.findItemCatList = function () {
        itemCatService.findAll().success(
            function (response) {
                for (var i = 0; i < response.length; i++) {
                    $scope.itemCatList[response[i].id] = response[i].name;
                }
            }
        );
    }
    $scope.checkAttributeValue = function (specName, optionName) {
        var items = $scope.entity.goodsDesc.specificationItems;
        var object = $scope.findObjectByKey(items, 'attributeName', specName);
        var object = $scope.findObjectByKey(items, 'attributeName', specName);
        if (object == null) {
            return false;
        } else {
            if (object.attributeValue.indexOf(optionName) >= 0) {
                return true;
            } else {
                return false;
            }

        }

    }
    $scope.save = function () {
        $scope.entity.goodsDesc.introduction = editor.html();
        var serviceObject;
        if ($scope.entity.goods.id != null) {
            serviceObject = goodsService.update($scope.entity);
        } else {
            serviceObject = goodsService.add($scope.entity);
        }
        serviceObject.success(
            function (response) {
                if (response.success) {
                    location.href="goods.html";//跳转到商品列表页
                    // alert("保存成功");
                    // $scope.entity={};
                    // editor.html("");

                }else {
                    alert(response.message);
                }
            }
        );
    }

});	
