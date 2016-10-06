/********* GoldCamKitPlugin.m Cordova Plugin Implementation *******/

#import <Cordova/CDV.h>
#import <GoldCameraKitEmbedded/GoldCameraKitEmbedded.h>

@interface GoldCamKitPlugin : CDVPlugin {
    // Member variables go here.
    GCKApplication *gckApp;
}

- (void)openCamera:(CDVInvokedUrlCommand*)command;
@end

@implementation GoldCamKitPlugin

- (void)openCamera:(CDVInvokedUrlCommand*)command
{
    __block CDVPluginResult* pluginResult = nil;
    
    if (gckApp == nil) {
        
        gckApp = [[GCKApplication alloc] initWithHandler:^(id sender) {
            NSLog(@"%@",sender);
            gckApp = nil;
            if (sender) {
                
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:sender];
                
            } else {
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Image not selected"];
                
            }
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
            
        }];
        
    }
    UINavigationController *navVC = gckApp.navController;
    //
    [[self topMostController] presentViewController:navVC animated:YES completion:nil];
    
}

- (UIViewController*) topMostController
{
    UIViewController *topController = [UIApplication sharedApplication].keyWindow.rootViewController;
    
    while (topController.presentedViewController) {
        topController = topController.presentedViewController;
    }
    
    return topController;
}


@end
