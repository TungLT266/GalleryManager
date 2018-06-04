package org.horaapps.leafpic.items;



public interface ActionsListener {


    void onItemSelected(int position);


    void onSelectMode(boolean selectMode);


    void onSelectionCountChanged(int selectionCount, int totalCount);
}
