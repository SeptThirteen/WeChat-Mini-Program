/**
 * 未登录统一引导:toast 只会一闪而过,老人不知道去哪登录。
 * 全站统一用弹窗确认后跳转登录页。
 */
export function promptLogin() {
  uni.showModal({
    title: '尚未登录',
    content: '登录后即可使用该功能',
    confirmText: '去登录',
    cancelText: '暂不',
    success: (res) => {
      if (res.confirm) {
        uni.navigateTo({ url: '/pages/login/login' });
      }
    }
  });
}
