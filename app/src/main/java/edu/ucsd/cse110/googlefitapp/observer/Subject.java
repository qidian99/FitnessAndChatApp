package edu.ucsd.cse110.googlefitapp.observer;

public interface Subject {
    void registerObserver(Observer o);

    void removeObserver(Observer o);

    void notifyObservers();

}
