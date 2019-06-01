package com.example.android.carassistant;

public interface IResolver<InputType, ResolvedType> {
    void resolve(InputType input);

    ResolvedType getResolved();
}
