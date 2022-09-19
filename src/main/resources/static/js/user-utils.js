function __humanReadable(num) {
    if (num < 1024) return (num) + " Bytes";
    if (num < 1024 * 1024) return Math.round(num / 1024) + " KBytes";
    if (num < 1024 * 1024 * 1024) return Math.round(num / 1024 / 1024)  + " MBytes";
    if (num < 1024 * 1024 * 1024 * 1024) return Math.round(num / 1024 / 1024 / 1024) + " GBytes";
    if (num < 1024 * 1024 * 1024 * 1024 * 1024) return Math.round(num / 1024 / 1024 / 1024 / 1024) + " TBytes";
    return num + " Wow, so big traffic";
}

function tableCreateOrUpdate(id, data, columns) {
    if ($.fn.dataTable.isDataTable(id)) {
        let datatable = $(id).DataTable();
        datatable.clear();
        datatable.rows.add(data);
        datatable.draw();
    } else {
        $(id).DataTable({
            "data" : data,
            "columns": columns,
            searching: false,
            paging: false,
            info: false
        });
    }
}
