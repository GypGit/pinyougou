app.service('brandService',function ($http) {
    this.findAll=function () {
        return $http.get("../brand/findAll.do");
    }
    this.findpage=function (page,rows) {
        return $http.get("../brand/findPage.do?page=" + page + "&rows=" + rows);
    }
    this.findOne=function (id) {
        return $http.post("../brand/findOne.do?id=" + id);
    }
    this.update=function (entity) {
        return $http.post("../brand/update.do", entity)
    }
    this.add=function (entity) {
        return $http.post("../brand/add.do", entity)
    }
    this.dele=function (ids) {
        return $http.get("../brand/delete.do?ids=" + ids)
    }
    this.search=function (page,rows,searchEntity) {
        return $http.post("../brand/search.do?page=" + page + "&rows=" + rows, searchEntity)
    }
    this.selectOptionList=function(){
        return $http.get('../brand/selectOptionList.do')
    }

});