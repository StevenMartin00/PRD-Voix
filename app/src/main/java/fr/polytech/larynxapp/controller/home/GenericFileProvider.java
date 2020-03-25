package fr.polytech.larynxapp.controller.home;

import androidx.core.content.FileProvider;

/**
 * Class used to open wav file on notification click
 * URI with file:// not working on newer versions of Android SDK
 * This class will replace file:// with content:// which is commonly used
 */
public class GenericFileProvider extends FileProvider
{

}
