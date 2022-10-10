package ru.sigsegv.dopamine.util;

import org.hibernate.ScrollableResults;

import java.util.ArrayList;
import java.util.List;

public class Paginated<T> {
    public List<T> data;
    public int currentPage;
    public int pageCount;

    public Paginated(List<T> data, int currentPage, int pageCount) {
        this.data = data;
        this.currentPage = currentPage;
        this.pageCount = pageCount;
    }

    public static <T> Paginated<T> fromScrollable(ScrollableResults<T> scrollable, int currentPage, int perPage) {
        scrollable.last();
        var numEntries = scrollable.getRowNumber() + 1;
        var pageCount = (int) Math.ceil((float) numEntries / perPage);
        if (pageCount == 0) pageCount = 1;
        if (currentPage >= pageCount) currentPage = pageCount - 1;
        if (currentPage < 0) currentPage = 0;

        scrollable.first();
        var hasAny = scrollable.scroll(currentPage * perPage);

        var list = new ArrayList<T>();
        while (hasAny && list.size() < perPage) {
            list.add(scrollable.get());
            if (!scrollable.next()) break;
        }

        return new Paginated<>(list, currentPage, pageCount);
    }
}
