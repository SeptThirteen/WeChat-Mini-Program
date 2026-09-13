# 生成 tabBar 图标(81x81, 4x超采样抗锯齿)
# 用法: python gen_tabbar_icons.py
from PIL import Image, ImageDraw
import os

OUT = os.path.join(os.path.dirname(__file__), "wechat-elderly-mini", "static", "tabbar")
os.makedirs(OUT, exist_ok=True)

SIZE = 324  # 4x of 81
NORMAL = (100, 116, 139, 255)   # #64748B
ACTIVE = (8, 145, 178, 255)     # #0891B2
WHITE = (255, 255, 255, 255)

def canvas():
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    return img, ImageDraw.Draw(img)

def home_icon(color):
    img, d = canvas()
    s = 4  # 所有坐标按 81 设计乘 4
    # 屋顶三角
    d.polygon([(40.5*s, 8*s), (5*s, 43*s), (76*s, 43*s)], fill=color)
    # 房身
    d.rectangle([13*s, 43*s, 68*s, 74*s], fill=color)
    # 门(镂空)
    d.rectangle([34*s, 52*s, 47*s, 74*s], fill=(0, 0, 0, 0))
    return img

def profile_icon(color):
    img, d = canvas()
    s = 4
    # 头
    d.ellipse([26*s, 8*s, 55*s, 37*s], fill=color)
    # 肩身(上半椭圆)
    d.ellipse([14*s, 42*s, 67*s, 95*s], fill=color)
    return img

def save(img, name):
    img = img.resize((81, 81), Image.LANCZOS)
    img.save(os.path.join(OUT, name), "PNG")
    print("saved", name)

save(home_icon(NORMAL), "home.png")
save(home_icon(ACTIVE), "home-active.png")
save(profile_icon(NORMAL), "profile.png")
save(profile_icon(ACTIVE), "profile-active.png")
print("done")
